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

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {
    
    final static Logger logger = LoggerFactory.getLogger(APIController.class);

    @CrossOrigin(origins = "http://localhost:8080")
    @RequestMapping("/api")
    @SuppressWarnings("static-method")
    public List<Question> getInitialSetOfQuestions() {
        logger.info("Retrieve initial set of questions");
        KnowledgeGraph instance = new KnowledgeGraph();
        return instance.retrieveFirstSetOfQuestions();
    }
    
    @CrossOrigin(origins = "http://localhost:8080")
    @RequestMapping(value="/api/child-questions", method=RequestMethod.POST, 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Question> getChildQuestions(@RequestBody Answer answer) {
        logger.info("Answered question with id: " + answer.getQuestionId() + 
                " with the following answer: " + answer.getAnswer());
        KnowledgeGraph instance = new KnowledgeGraph();
        return instance.retrieveChildQuestions(answer);
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @RequestMapping(value="/api/suggestions", method=RequestMethod.POST, 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Suggestion> getsuggestions(@RequestBody List<Answer> answers) {
        logger.info("Get suggestions given the following answeres: [ " + 
                answers.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) + " ]");

        KnowledgeGraph instance = new KnowledgeGraph();
        return instance.retrieveSuggestions(answers);
    }
    
}
