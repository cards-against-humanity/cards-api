package elasticsearch

import database.DatabaseCollection

interface SearchableDatabaseCollection : DatabaseCollection, ElasticSearcher, ElasticAutoCompleter