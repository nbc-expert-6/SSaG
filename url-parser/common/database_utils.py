from sqlalchemy import create_engine, MetaData, Table
from sqlalchemy.orm import Session

# Database 연결 설정
class Database:
    def __init__(self, url):
        self.engine = create_engine(url)
        self.metadata = MetaData()

    def load_table(self, table_name, schema=None):
        return Table(
            table_name,
            self.metadata,
            schema=schema,
            autoload_with=self.engine
        )

    def session(self):
        return Session(self.engine)

    def connect(self):
        return self.engine.connect()
