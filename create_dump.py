import getpass
import os
import subprocess

# Variables
DB_USER = "root"
DB_NAME = "ventepizza"
DUMP_FILE = "./sql/script/ventepizza-dump.sql"


# Check if mysqldump is installed
def check_mysqldump():
    try:
        subprocess.run(["mysqldump", "--version"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except subprocess.CalledProcessError:
        print("mysqldump could not be found. Please install MySQL client tools.")
        return False
    return True


# Create the dump
def create_dump(db_password):
    try:
        # Remove the dump file if it exists
        if os.path.exists(DUMP_FILE):
            os.remove(DUMP_FILE)
            print(f"Existing dump file {DUMP_FILE} removed.")

        # Dump views
        get_views_cmd = [
            "mysql", "-u", DB_USER, "-p" + db_password, "INFORMATION_SCHEMA",
            "--skip-column-names", "--batch",
            "-e",
            f"select table_name from information_schema.tables where table_type = 'VIEW' and table_schema = '{DB_NAME}'"
        ]
        get_views = subprocess.run(get_views_cmd, capture_output=True, text=True, check=True)
        views = get_views.stdout.split()
        dump_views_cmd = ["mysqldump", "-u", DB_USER, "-p" + db_password, DB_NAME] + views

        with open(DUMP_FILE, "w") as dump_file:
            if views:
                subprocess.run(dump_views_cmd, check=True, stdout=dump_file)
            subprocess.run(
                ["mysqldump", "--no-create-info", "--no-data", "--routines", "--no-create-db", "--skip-triggers", "-u",
                 DB_USER, f"-p{db_password}", DB_NAME],
                check=True, stdout=dump_file)
        print(f"Database dump created successfully: {DUMP_FILE}")
    except subprocess.CalledProcessError:
        print("Failed to create database dump.")


if __name__ == "__main__":
    if check_mysqldump():
        db_password = getpass.getpass(prompt="Enter MySQL root password: ")
        create_dump(db_password)
