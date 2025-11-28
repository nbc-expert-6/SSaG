FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-nori

USER elasticsearch8.11.0