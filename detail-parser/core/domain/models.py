from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Dict, List, Optional


@dataclass(frozen=True)
class BatchMessage:
    main_product_id: str
    urls: List[str]
    raw: Dict[str, Any]


@dataclass(frozen=True)
class DetailInput:
    main_product_id: str
    url: str


@dataclass(frozen=True)
class DetailResult:
    ok: bool    # 수정 필요
    elapsed_sec: float
    error_type: Optional[str] = None
    error_message: Optional[str] = None
    stage: Optional[str] = None
    reason: Optional[str] = None
    payload: Optional[Dict[str, Any]] = None  # 성공하면 product_details
