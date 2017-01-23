/*
Copyright (c) 2017 Cycledev AB

Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.cycledev.cohints;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Retrieves data from the knowledge graph.
 *
 * @author Leonard Saers
 */
@Repository
public class KnowledgeGraph {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(KnowledgeGraph.class);

    private TransportClient client;

    private static String ELASTICSEARCH_URL = "elastic";
    private static String INDEX_NAME = "co2hints";
    private static String INDEX_TYPE_QUESTION = "question";
    private static String INDEX_TYPE_SUGGESTION = "suggestion";

    public KnowledgeGraph() {
        try {
            this.client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress
                        (new InetSocketTransportAddress
                            (InetAddress.getByName(ELASTICSEARCH_URL), 9300));
        } catch (UnknownHostException ex) {
            logger.error("Failed to connect to elasticsearch", ex);
        }
    }
    
    public KnowledgeGraph(TransportClient client) {
        this.client = client;
    }

    /**
     * Retrieves the initial list of {@link Question}.
     *
     * @return a list of initial {@link Question}
     */
    public List<Question> retrieveFirstSetOfQuestions() {
        QueryBuilder parentQuestionQuery = boolQuery()
                .mustNot(QueryBuilders.existsQuery("parent-question"));

        ScoreFunctionBuilder scoreFunction
                = ScoreFunctionBuilders.scriptFunction("doc['impact'].value");
        parentQuestionQuery
                = QueryBuilders.functionScoreQuery(parentQuestionQuery, scoreFunction);

        Function<SearchResponse, List<Question>> searchResponceConsumer
                = (SearchResponse sr) -> {
                    return parseQuestionsFromSearchResult(sr);
                };

        return getSearchResults(scoreFunction,
                parentQuestionQuery,
                INDEX_TYPE_QUESTION,
                searchResponceConsumer,
                Question.class);
    }

    /**
     * Retrieves child {@link Question}s to a given {@link Answer} 
     * 
     * @param parentAnswer the parent {@link Answer}
     * @return list of child {@link Question} for the given {@link Answer}
     */
    public List<Question> retrieveChildQuestions(Answer parentAnswer) {
        QueryBuilder parentQuestionQuery = boolQuery()
                .must(matchAllQuery())
                .filter(termQuery("parent-question", parentAnswer.getQuestionId()))
                .filter(termQuery("parent-answer", parentAnswer.getAnswer()));

                ScoreFunctionBuilder scoreFunction
                = ScoreFunctionBuilders.scriptFunction("doc['impact'].value");
        parentQuestionQuery = 
                QueryBuilders.functionScoreQuery(parentQuestionQuery, scoreFunction);

        Function<SearchResponse, List<Question>> searchResponceConsumer
                = (SearchResponse sr) -> {
                    return parseQuestionsFromSearchResult(sr);
                };

        return getSearchResults(scoreFunction,
                parentQuestionQuery,
                INDEX_TYPE_QUESTION,
                searchResponceConsumer,
                Question.class);
    }
    
    public List<Suggestion> retrieveSuggestions(List<Answer> answers) {

        BoolQueryBuilder includeSuggestionsQuery = boolQuery();
        BoolQueryBuilder excludeSuggestionsQuery = boolQuery();

        Consumer<Answer> includeSuggestionQueryConsumer = answer -> {
            BoolQueryBuilder includeSuggestion = boolQuery();

            includeSuggestion.must(
                    termQuery(
                            "dependent_question.answer",
                            answer.getAnswer()));
            includeSuggestion.must(
                    termQuery(
                            "dependent_question.question_id",
                            answer.getQuestionId()));

            includeSuggestionsQuery.should(includeSuggestion);
        };

        Consumer<Answer> excludeSuggestionQueryConsumer = answer -> {
            excludeSuggestionsQuery.must(
                    termQuery("dependent_question.answer",
                            answer.getAnswer()));
            excludeSuggestionsQuery.must(
                    termQuery("dependent_question.question_id",
                            answer.getQuestionId()));
        };

        createDependentQuestionConstraintsForSuggestionsQuery(
                answers,
                includeSuggestionQueryConsumer,
                excludeSuggestionQueryConsumer);

        QueryBuilder nestedIncludeQuery = nestedQuery("dependent_question",
                includeSuggestionsQuery, ScoreMode.None);

        QueryBuilder nestedExcludeQuery = nestedQuery("exclude_question",
                excludeSuggestionsQuery, ScoreMode.None);

        QueryBuilder fullQuery = boolQuery()
                .must(nestedIncludeQuery)
                .mustNot(nestedExcludeQuery);

        ScoreFunctionBuilder scoreFunction = 
                ScoreFunctionBuilders.scriptFunction
                    ("doc['impact'].value / doc['simplicity'].value");

        fullQuery = QueryBuilders.functionScoreQuery(fullQuery, scoreFunction);

        Function<SearchResponse, List<Suggestion>> searchResponceConsumer
        = (SearchResponse sr) -> {
            return parseSuggestionsFromSearchResult(sr);
        };

        return getSearchResults(scoreFunction,
                fullQuery,
                INDEX_TYPE_SUGGESTION,
                searchResponceConsumer,
                Suggestion.class);
    }
    
    private <T> List<T> getSearchResults(
            ScoreFunctionBuilder scoreFunction,
            QueryBuilder queryBuilder,
            String indexType,
            Function<SearchResponse, List<T>> searchResponceConsumer,
            Class<T> resultType) {

        SearchResponse response = client.prepareSearch(INDEX_NAME)
                .setTypes(indexType)
                .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
                .setQuery(queryBuilder).get();

        return searchResponceConsumer.apply(response);
    }

    private List<Question> parseQuestionsFromSearchResult(SearchResponse response) {
        List<Question> questions = new LinkedList<>();

        for (SearchHit hit : response.getHits()) {
            List<HashMap> l = (List<HashMap>) hit.getSource().get("options");

            List<Option> options = new LinkedList<>();

            for (HashMap map : l) {
                logger.info(l.toString());
                options.add(new Option(map.get("option").toString(),
                        map.get("statement").toString()));
            }

            questions.add(
                    new Question(hit.getSource().get("question").toString(),
                            options,
                            hit.getId()));
        }

        return questions;
    }

    private List<Suggestion> parseSuggestionsFromSearchResult(SearchResponse response) {
        List<Suggestion> suggestions = new LinkedList<>();

        for (SearchHit hit : response.getHits()) {
            suggestions.add(
                    new Suggestion(
                            (String) hit.getSource().get("suggestion"),
                            (String) hit.getSource().get("text"),
                            (Integer) hit.getSource().get("impact"),
                            (Integer) hit.getSource().get("simplicity"))
            );

        }

        return suggestions;
    }



    private void
            createDependentQuestionConstraintsForSuggestionsQuery(
                    List<Answer> answers,
                    Consumer<Answer> includeSuggestionQueryConsumer,
                    Consumer<Answer> excludeSuggestionQueryConsumer) {
        for (Answer answer : answers) {
            includeSuggestionQueryConsumer.accept(answer);
            excludeSuggestionQueryConsumer.accept(answer);
        }
    }
}
