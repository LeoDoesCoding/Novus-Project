# Novus-Project (AKA The CRUDinator)
(Project assesment for Novus training program. Assesed point: https://github.com/LeoDoesCoding/Novus-Project/commit/efb718200c5dc8b84441f1c7e84e26f903f869e1)

Prerequisites: User must have an established connection to an SQL server for this application to run as intended.
To run the code, libraries hynnet.sqljdbc4.chs and Javafx 20.0.02 must be used.

This program is an interface for manual SQL query entry, taking a visual approach to database modification similar to Excel. It converts visual table entries into SQL queries, meaning a user does not need to know or recall query formats and the table's constraints to add, modify and remove entries from a table. Changes to a database are NOT automatically sent, rather are only produced and sent when a user clicks save, allowing changes to be discarded safely.


## Version 0.1
__9th November 2023 - The project as assessed in demonstration.__

Column and rows can be added to tables, and modifications to entries can be made.
There is no input validation and new columns cannot be configured (they automatically created as a varchar with an auto-generated name) and columns and rows cannot be deleted.
