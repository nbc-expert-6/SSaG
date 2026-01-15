# core/app/restart_policy.py
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class BatchRestartPolicy:
    max_batch: int

    def should_restart(self, processed_count: int) -> bool:
        if self.max_batch <= 0:
            return False
        return processed_count > 0 and processed_count % self.max_batch == 0

    def get_processed_count(self, processed_count: int) -> str:
        return f"processed_count={processed_count}, max_batch={self.max_batch}"
