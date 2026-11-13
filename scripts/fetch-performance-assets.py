"""Install the pinned ARM64 Java runtime and Holy GL4ES binary."""

import argparse
import hashlib
from pathlib import Path
import tempfile
from urllib.request import urlretrieve
from zipfile import ZipFile


BASE_RELEASE = (
    "https://github.com/Minehacker765/Amethyst-Android-Offline-Support/"
    "releases/download/v2026.07.19.0341/app-release.apk"
)
BASE_SHA256 = "38f77f69f1e9de95e1a831dd43a1ec3a6cbabd67928a5e8a3c79ad93c6a92f73"
HOLY_URL = (
    "https://raw.githubusercontent.com/PojavLauncherTeam/PojavLauncher/"
    "3912db6e219ba671e7da0a949053d8fc428eb320/"
    "app_pojavlauncher/src/main/jniLibs/arm64-v8a/libgl4es_114.so"
)
HOLY_SHA256 = "3dbcc87baae1436f2affb923eda053d7e256f0aa032654452e2b2092566f6b24"
JRE_FILES = {
    "bin-arm64.tar.xz": "28385a2aac44382af2c4f6333484fee80228e559d8086f210a9c57cb41051ec6",
    "universal.tar.xz": "464afec94a002cb9a3d20364f3f218d35d177c198b9ac89d35255514985f5eb7",
    "version": "a48816f24d4791a09616260e274667cf9bc5c7779548388ebf1d8ca0660a31cc",
}
MAIN = Path(__file__).resolve().parents[1] / "app_pojavlauncher" / "src" / "main"
JRE_PATH = MAIN / "assets" / "components" / "jre"
HOLY_PATH = MAIN / "jniLibs" / "arm64-v8a" / "libgl4es_114.so"


def matches_hash(path, expected):
    if not path.is_file():
        return False
    with path.open("rb") as source:
        return hashlib.file_digest(source, "sha256").hexdigest() == expected


def read_verified(path, expected):
    if not matches_hash(path, expected):
        raise ValueError(f"SHA-256 mismatch: {path}")
    return path


def install(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(dir=path.parent, delete=False) as output:
        temporary_path = Path(output.name)
    try:
        temporary_path.write_bytes(data)
        temporary_path.replace(path)
    finally:
        temporary_path.unlink(missing_ok=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--upstream-apk", type=Path, help="Reuse a local copy of the pinned upstream APK")
    parser.add_argument("--holy-library", type=Path, help="Reuse a local copy of the pinned Holy GL4ES library")
    args = parser.parse_args()

    try:
        with tempfile.TemporaryDirectory() as directory:
            temporary = Path(directory)
            if any(not matches_hash(JRE_PATH / name, digest) for name, digest in JRE_FILES.items()):
                apk_path = args.upstream_apk
                if apk_path is None:
                    apk_path = temporary / "app-release.apk"
                    urlretrieve(BASE_RELEASE, apk_path)
                with ZipFile(read_verified(apk_path, BASE_SHA256)) as apk:
                    for name, expected in JRE_FILES.items():
                        data = apk.read(f"assets/components/jre/{name}")
                        if hashlib.sha256(data).hexdigest() != expected:
                            raise ValueError(f"SHA-256 mismatch for the Java runtime asset: {name}")
                        install(JRE_PATH / name, data)

            if not matches_hash(HOLY_PATH, HOLY_SHA256):
                library = args.holy_library
                if library is None:
                    library = temporary / HOLY_PATH.name
                    urlretrieve(HOLY_URL, library)
                install(HOLY_PATH, read_verified(library, HOLY_SHA256).read_bytes())
    except (OSError, ValueError, KeyError) as error:
        parser.exit(1, f"Could not prepare build assets: {error}\n")

    print("ARM64 Java 8 and Holy GL4ES assets verified.")


if __name__ == "__main__":
    main()
