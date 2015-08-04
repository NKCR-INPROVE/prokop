# PROKOP #
## Analytický nástroj pro kontrolu konzistence bibliografických dat ##

Záměrem projektu je vytvoření analytického nástroje pro zpracování bibliografických dat napříč spektrem systémů využívaných předními knihovnami v České republice a jejich spolupracujícími institucemi.
V rámci analýz a výzkumu možných optimalizací předpokládáme vznik sady nástrojů pro sběr/harvesting dat z bází velkých knihoven, digitálních knihoven a Registru digitalizace. Data budou normalizována na společný profil, který následně umožní pomocí definovatelných algoritmů analyzovat a vyhodnocovat souvislosti.
Vyhodnocování bude probíhat jak mezi více zdroji, tak v rámci jednoho samostatného datového vstupu – například Báze knihovny.

Principiálně se prostředí PROKOP bude skládat z DB/FS pracovního prostoru, do kterého se budou sklízet data z porovnávaných zdrojů. K nim bude přistupovat modul využívající jednotlivé „Horníky“ v podobě Java programů, SQL procedur, SQL dotazů apod.
Výstupem by bylo zjištění shody, rozdílu, chybějících dat apod. Kromě výstupu do souboru a statistiky by mohl proces končit zavoláním API cílového systému – pro následné provedení optimalizace/opravy.

Vzhledem k předpokládanému zpracovávání velkého objemu dat, považujeme za optimální systém navrhnout jako škálovatelný s využitím paralelního zpracování dat v distribuovaném prostředí.
PROKOP by tak mohl pracovat, jak na požádání o provedení určité srovnávací operace, tak jako permanentní služba sklízející data s napojených zdrojů s využitím OAI protokolu.

Předpokládáme úzké navázání projektu PROKOP na Registr digitalizace, kde by jeho přispění mohlo přinést další úroveň sjednocování aktivit v oblasti digitalizace českého kulturního dědictví.
