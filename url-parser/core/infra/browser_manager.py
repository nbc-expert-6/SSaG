# core/infra/browser_manager.py
from __future__ import annotations

import logging
from typing import Callable, Generic, Optional, TypeVar

TParser = TypeVar("TParser")


class BrowserManager(Generic[TParser]):
    def __init__(self, parser_factory: Callable[[], TParser]):
        self._factory = parser_factory
        self._parser: Optional[TParser] = None

    def get(self) -> TParser:
        if self._parser is None:
            self._parser = self._factory()
        return self._parser

    def restart(self, processed_count: str) -> None:
        self.quit()
        self._parser = self._factory()

    def quit(self) -> None:
        if self._parser is None:
            return
        try:
            quit_fn = getattr(self._parser, "quit", None)
            if callable(quit_fn):
                quit_fn()
        finally:
            self._parser = None
