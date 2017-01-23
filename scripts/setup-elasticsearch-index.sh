#!/bin/bash
curl -XPUT "http://localhost:9200/co2hints" -d'
{
   "mappings": {
      "question": {
         "properties": {
            "options": {
               "properties": {
                  "option": {
                     "type": "text",
                     "fields": {
                        "keyword": {
                           "type": "keyword",
                           "ignore_above": 256
                        }
                     },
                     "store": "yes",
                     "index": "analyzed"
                  },
                  "statement": {
                     "type": "text",
                     "fields": {
                        "keyword": {
                           "type": "keyword",
                           "ignore_above": 256
                        }
                     },
                     "store": "yes",
                     "index": "analyzed"
                  }
               }
            },
            "parent-question": {
               "type": "text",
               "store": "yes",
               "index": "analyzed"
            },
            "parent-answer": {
               "type": "keyword",
               "store": "yes",
               "index": "not_analyzed",
               "fields": {
                  "keyword": {
                     "type": "keyword",
                     "ignore_above": 256,
                     "index": "not_analyzed"
                  }
               }
            },
            "question_impact": {
               "type": "integer",
               "store": "yes",
               "index": "analyzed"
            }
         }
      },
      "suggestion": {
         "properties": {
            "suggestion_impact": {
               "type": "integer"
            },
            "suggestion_simplicity": {
               "type": "long"
            },
            "dependent_question": {
               "type": "nested",
               "properties": {
                  "answer": {
                     "type": "keyword"
                  },
                  "question_id": {
                     "type": "keyword"
                  }
               }
            },
            "exclude_question": {
               "type": "nested",
               "properties": {
                  "answer": {
                     "type": "keyword"
                  },
                  "question_id": {
                     "type": "keyword"
                  }
               }
            }
         }
      }
   }
}'

curl -XPOST http://127.0.0.1:9200/_bulk?pretty=true --data-binary @knowledgegraph-bulk.json
