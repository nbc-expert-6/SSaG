# core/app/restart_policy.py
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class PoolRestartPolicy:
    max_batches: int

    def should_restart(self, processed_batch_count: int) -> bool:
        if self.max_batches <= 0:
            return False
        return processed_batch_count > 0 and processed_batch_count % self.max_batches == 0

    def reason(self, processed_batch_count: int) -> str:
        return f"processed_batch_count={processed_batch_count}, max_batches={self.max_batches}"
