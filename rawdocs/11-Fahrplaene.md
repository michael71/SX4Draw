# SX4Draw - Fahrpläne

Fahrpläne sind eine Abfolge von Fahrten, ein "Name" dient zur leichteren Identifizierung.

Eine Bemerkung: natürlich müssen die Fahrten so kombiniert werden, 
dass bei jeder Fahrt die **richtige** Lok auf dem **Anfangssensor** der Fahrstraße steht!

Sie können im SX4 Programm einen Fahrplan (der im panel.xml File enthalten sein muss) automatisch ablaufen lassen – dann 
werden die Fahrten in der gespeicherten  Reihenfolge ausgeführt – vorausgesetzt, auf den Start-Sensoren 
der jeweiligen Fahrstraße steht jeweils ein Zug (sonst weiß das Programm nicht, welche Lok es beschleunigen
 oder abbremsen soll).

Wichtig: zunächst müssen die **Fahrstraßen** erstellt worden sein!

Mit dem Button **"+ Fahrplan"** kann ein neuer Fahrplan erstellt werden.

1. zunächst wird ein Name für den neuen Fahrplan erfragt

2. dann wird der "Start-Button" angeklickt (und dann mit "1" markiert)

3. daraufhin werden die möglichen Ziele (Ende der Fahrstraße) mit "2" markiert

4. nach Auswahl des Ziels wird die gewählte Fahrstrasse angezeigt

5. die zu startende Lok (bzw Zug) mit Adresse, Richtung und Geschwindigkeit muss gewählt werden

6. dann kann der Fahrplan abgeschlossen werden - oder es wird eine weitere Fahrt hinzugefügt (-> weiter bei Punkt 2)

Der erzeugte Fahrplan (und die darin enthaltenen Fahrten) werden dann mit dem nächsten "Panel abspeichern" 
im panel.xml abgespeichert.

Tabellarisch sehen Fahrpläne wie folgt aus:

![](img22a.png)



![](img22.png)



-> Weiter zu [Select](12-Select.md)

-> Zurück zum [Index](index.md)
