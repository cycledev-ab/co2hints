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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Leonard Saers
 */
public class KnowledgeGraphTest {

    public KnowledgeGraphTest() {
    }

    /**
     * Test of retrieveFirstSetOfQuestions method, of class KnowledgeGraph.
     */
    @Test
    public void testRetrieveFirstSetOfQuestions() {
        TransportClient client = createTransportClientMock(createHitQuestionData());
        KnowledgeGraph graph = new KnowledgeGraph(client);
        List<Question> questions = graph.retrieveFirstSetOfQuestions();
        
        assertEquals(questions.get(0).getQuestion()
                , "How much coffy do you drink?");
        assertEquals(questions.get(0).getOptions().get(0).getOption()
                , "10 cups per day");
        assertEquals(questions.get(0).getOptions().get(0).getStatement()
                , "I dring 10 cups coffy per day");
        assertEquals(questions.get(0).getId(), "1");
    }
    
    @Test
    public void testRetrieveChildQuestions() {
        TransportClient client = createTransportClientMock(createHitQuestionData());
        KnowledgeGraph graph = new KnowledgeGraph(client);
        List<Question> questions = 
                graph.retrieveChildQuestions(new Answer("1", "test"));
        
        assertEquals(questions.get(0).getQuestion()
                , "How much coffy do you drink?");
        assertEquals(questions.get(0).getOptions().get(0).getOption()
                , "10 cups per day");
        assertEquals(questions.get(0).getOptions().get(0).getStatement()
                , "I dring 10 cups coffy per day");
        assertEquals(questions.get(0).getId(), "1");
    }
    
    @Test
    public void testRetrieveSuggestions() {
        TransportClient client = createTransportClientMock(createHitSuggestionData());
        KnowledgeGraph graph = new KnowledgeGraph(client);
        
        List<Answer> selectedSuggestions = new LinkedList<>();
        selectedSuggestions.add(new Answer("1", "10 cups per day"));
        
        List<Suggestion> answers = 
                graph.retrieveSuggestions(selectedSuggestions);
        
        assertEquals(answers.get(0).getSuggestion()
                , "Drink more coffy");
        assertEquals(answers.get(0).getText()
                , "Increases your coffy consumption");
        assertEquals(answers.get(0).impact
                , 2);
        assertEquals(answers.get(0).simplicity
                , 50);
    }

    private TransportClient createTransportClientMock(Map<String, Object> hitsSources) {
        TransportClient client = mock(TransportClient.class);
        SearchResponse response = mock(SearchResponse.class);
        SearchRequestBuilder requestBuilder = mock(SearchRequestBuilder.class);
        
        when(client.prepareSearch(Matchers.any())).thenReturn(requestBuilder);
        when(requestBuilder.setQuery(Matchers.any())).thenReturn(requestBuilder);
        when(requestBuilder.setTypes(Matchers.any())).thenReturn(requestBuilder);
        when(requestBuilder.setSearchType(SearchType.DFS_QUERY_AND_FETCH)).thenReturn(requestBuilder);
        when(client.prepareSearch(Matchers.any())
                .setTypes(Matchers.any())
                .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
                .setQuery(Matchers.any()).get())
                .thenReturn(response);
        
        SearchHit hit = mock(SearchHit.class);
        List<SearchHit> hits = new LinkedList<>();
        hits.add(hit);
        SearchHits searchHits = mock(SearchHits.class);
        
        when(searchHits.iterator()).thenReturn(hits.iterator());
        when(response.getHits()).thenReturn(searchHits);
        
        when(hit.getId()).thenReturn("1");
        when(hit.getSource()).thenReturn(hitsSources);
        
        return client;
    }

    private Map<String, Object> createHitQuestionData() {
        Map<String, Object> hitsSources = new HashMap<>();
        List<HashMap> options = new LinkedList<>();
        HashMap optionsValue = new HashMap();
        optionsValue.put("option", "10 cups per day");
        optionsValue.put("statement", "I dring 10 cups coffy per day");
        options.add(optionsValue);
        hitsSources.put("options", options);
        hitsSources.put("question", "How much coffy do you drink?");
        return hitsSources;
    }
    
    private Map<String, Object> createHitSuggestionData() {
        Map<String, Object> hitsSources = new HashMap<>();
        List<HashMap> options = new LinkedList<>();
        HashMap dependentQuestion = new HashMap();
        dependentQuestion.put("answer", "I dring 10 cups coffy per day");
        dependentQuestion.put("question_id", "1");
        options.add(dependentQuestion);
        
        hitsSources.put("impact", 2);
        hitsSources.put("simplicity", 50);
        
        hitsSources.put("suggestion", "Drink more coffy");
        hitsSources.put("text", "Increases your coffy consumption");
        return hitsSources;
    }

}
