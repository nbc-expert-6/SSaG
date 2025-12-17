FROM docker.elastic.co/elasticsearch/elasticsearch:8.19.0

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-nori

USER elasticsearch