from sqlalchemy import MetaData, Table, create_engine
from sqlalchemy.orm import Session

from common.config import DATABASE_URL


# Database 연결 설정
class Database:
    def __init__(self):
        self.url = DATABASE_URL
        self.engine = create_engine(self.url)
        self.metadata = MetaData()

    def load_table(self, table_name):
        return Table(
            table_name,
            self.metadata,
            autoload_with=self.engine
        )

    def session(self):
        return Session(self.engine)

    def connect(self):
        return self.engine.connect()
