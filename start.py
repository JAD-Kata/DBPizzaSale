import getpass
import subprocess

# Variables
DB_USER = "root"
DB_NAME = "ventepizza"
DUMP_FILE = "./sql/script/ventepizza.sql"


def warningUser():
    print("Ce script va créer la base de données ventepizza et y insérer des données.")
    print("Toutes les données actuelles de la base de données ventepizza seront perdues.")
    print("Etes-vous sûr de vouloir continuer ? (o/n)")
    answer = input()
    return answer == "o"


def executeSqlScript(sql_file, db_password):
    try:
        subprocess.run(
            ["mysql", "-u", DB_USER, f"-p{db_password}", "-e", f"source {sql_file}"],
            check=True
        )
        print(f"Base de données créée avec succès.")
    except subprocess.CalledProcessError:
        print("Impossible de créer la base de données.")
        print("Veuillez vérifier que le fichier de dump existe et que le mot de passe est correct.")
        print(f"Dump file: {sql_file}")


if __name__ == "__main__":
    if warningUser():
        db_password = getpass.getpass(prompt="Enter MySQL root password: ")
        executeSqlScript(DUMP_FILE, db_password)
