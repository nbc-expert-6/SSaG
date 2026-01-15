# core/domain/models.py
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Dict, List, Optional


@dataclass(frozen=True)
class CrawlInput:
    main_product_id: str
    keyword: str
    raw: Dict[str, Any]


@dataclass(frozen=True)
class CrawlResult:
    urls: List[str]
    url_cnt: int
    elapsed_sec: float

