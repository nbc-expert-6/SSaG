# core/infra/browser_manager.py
from __future__ import annotations

from contextlib import contextmanager
from queue import Empty, Queue
from typing import Callable, Generator, Generic, Optional, TypeVar

TParser = TypeVar("TParser")


class DriverPoolManager(Generic[TParser]):
    def __init__(self, parser_factory: Callable[[], TParser], pool_size: int):
        if pool_size <= 0:
            raise ValueError("pool_size must be > 0")
        self._factory = parser_factory
        self._pool_size = pool_size
        self._pool: Queue[TParser] = Queue(maxsize=pool_size)
        self._initialized = False

    def init_pool(self) -> None:
        if self._initialized:
            return
        for _ in range(self._pool_size):
            self._pool.put(self._factory())
        self._initialized = True

    def shutdown_pool(self) -> None:
        if not self._initialized:
            return
        while True:
            try:
                parser = self._pool.get_nowait()
            except Empty:
                break
            try:
                quit_fn = getattr(parser, "quit", None)
                if callable(quit_fn):
                    quit_fn()
            except Exception:
                pass
        self._initialized = False

    # parser를 빌려서 쓰고, 반납하게 하는 컨텍스트 매니저
    @contextmanager
    def acquire(self) -> Generator[TParser, None, None]:
        parser: Optional[TParser] = None
        try:
            parser = self._pool.get()  # 없으면 block
            yield parser
        finally:
            if parser is not None:
                self._pool.put(parser)
