import importlib


class DetailParserResolver:
    @staticmethod
    def resolve(platform: str):
        platform_lc = platform.lower()
        class_name = f"{platform}DetailParser"
        module_path = f"crawler.{platform_lc}.{platform_lc}_detail_parser"

        try:
            module = importlib.import_module(module_path)
            return getattr(module, class_name)
        except (ModuleNotFoundError, AttributeError) as e:
            raise RuntimeError(
                f"platform이 존재하지 않거나 DetailParser가 존재하지 않습니다.: {platform}"
            ) from e