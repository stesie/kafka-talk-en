---
title: Kafka 101
separator: <!--s-->
verticalSeparator: <!--v-->
---

# Kafka 101

Stefan Siegl (@stesie23, <rolf@mayflower.de>)

<!--s-->

# Kafka Basics

![Kafka Overview](kafka-apis.png)

<!--v-->

# Nachrichten

* "Messages"
* bestehen aus Key & Value
* grundsätzlich Binärdaten
* typischerweise: JSON oder Avro

<!--v-->

# Topics

* ... eine "Kategorie" von Nachrichten
* (Schnittgrenze ist in etwa gleich wie DDD Aggregate)
* ... sind unterteilt in Partitionen

<!--v-->

# Partitionen

* "commit log" für neue Nachrichten
* werden nur am Ende beschrieben
* nur innerhalb Partition besteht Ordnung
* ist Basis für Replikation
* 1 Leader, 0..n Follower

<!--v-->

![Aufbau eines Topics mit Partitionen](topic_anatomy.png)

<!--v-->

# Producer

* veröffentlicht eine Nachricht auf ein Topic
* Producer legt fest, auf welche Partition ID
* Nachricht ohne Key, üblicherweise round-robin
* mit Key, üblicherweise auf Basis Hash über Key

<!--v-->

# Consumer

* holt Nachrichten von Topic
* Lesefortschritt ("Offset") wird von Consumer verwaltet(!)
   * via Commit auf internem Topic
* Nachrichten verbleiben nach dem Lesen im Topic
* beliebig viele Consumer auf einem Topic

<!--v-->

![Consumer können an verschiedenen Offsets lesen](log_consumer.png)

<!--v-->

# Consumer Groups

* "Label" unter dem sich mehrere Consumer untereinander abstimmen
* letztlich nur eine Zeichenfolge
* Verteilung erfolgt auf Basis von Partitionen
* d.h. niemals zwei Consumer für eine "halbe" Partition

<!--v-->

![zwei Consumer Groups](consumer-groups.png)

<!--v-->

# Anzahl Partitionen

* nur (sehr) schwer änderbar
* so viele, dass man weiter skalieren kann
* Anzahl sollte restlos durch Anzahl Consumer teilbar sein, sonst Last-Ungleichgewicht
* Richtwert: 12 oder 40

<!--v-->

# Log Compaction

* was passiert mit "alten" Nachrichten?
* zwei "Arten" von Nachrichten
   * Event Stream
   * Aggregierte Daten
* Log Compaction streicht alte Nachrichten mit dem gleichen Key

<!--v-->

![Visualisierung Log Cleaner Vorgehen](log_cleaner_anatomy.png)

<!--s-->

# Usage Pattern
<!--v-->

## Queue

* d.h. ein Prozess kippt Tasks in eine Queue und Worker arbeiten ab
* beliebig viele Services sind Teil *einer* Consumer Group
* wenn die Tasks stateless sind, dann ohne Key (-> round-robin Verteilung)
* wenn Worker Events aggregieren, dann gruppierendes Attribut als Key

<!--v-->

## Pub/Sub

* d.h. Event wird publiziert und es gibt eine Reihe an interessierten Services
* jeder der Services ist in einer *eigenen* Consumer Group
* Konsequenz: jeder Service erhält das Event zur Verarbeitung

<!--v-->

## Mischfälle

* z.B. Events werden publiziert, an denen mehrere Services interessiert sind + Lastverteilung
* jede Art von Service in *eigener* Consumer Group
* alle Services die parallel zur Lastverteilung laufen, sind *in der gleichen* Consumer Group

<!--v-->

## Replay

* nachdem die Consumer den Offset selbst verwalten, können sie auch einfach nochmal von vorn lesen
* neuer Service, der existierende Events verarbeiten soll, kann bestimmen ob er von Anfang oder ab aktuellem Ende konsumieren will

<!--s-->

# Skalierbarkeit

* bei Kafka sind die Broker vergleichsweise doof
* Großteil der Arbeit machen Producer & Consumer
* Konsequenz: Unterschiede im Funktionsumfang der Client-Libraries (z.B. Java vs. PHP)
   * z.B. produced `nmred/kafka-php` immer round robin

<!--s-->

# TL;DR

* Kafka ist ganz witzig, wenn das Erhaltenbleiben von Nachrichten wichtig ist
* ... oder wenn man die Skalierbarkeit braucht
* im Java-Umfeld macht das auch eher Spaß (-> kafka-streams)

<!--s-->

# kafka-streams

<!--v-->

## kafka-streams

* Java Library für stream processing
* kümmert sich um
   * Verteilung auf Instanzen
   * Fehlerbehandlung
   * Windowing
   * State-Verwaltung

<!--v-->

## zwei APIs

* Streams DSL (= high-level)
* Processor API (= low-level)

<!--s-->

# Beispiele

<!--v-->

## Stateless Processor

* quasi der einfachste Fall
* Topologie beginnt bei einem Topic (oder mehreren)
* die Streams DSL bietet die üblichen Primitiven a la `filter` & `map`
* Ergebnis kann man
   * verarbeiten (`foreach`)
   * wieder veröffentlichen (`to`)

<!--v-->

### Beispiel

* Ziel ist die Auswertung eines nginx access logs
* im ersten Schritt soll jede Zeile geparsed und das wieder publiziert werden

<!--v-->

```java
    @Bean
    public KStream<?, ?> build(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder
                .stream("streamsdemo.logparser.raw", Consumed.with(null, new StringSerde()));

        stream
            .mapValues(LogEntrySpecification::of)
            .filter((k, v) -> v != null)
            .selectKey((k, v) -> v.getIpAddress())
            .to("streamsdemo.logparser.entries", 
                Produced.with(new StringSerde(), new JsonSerde<>(LogEntry.class)));

        return stream;
    }
```

<!--v-->

## Stateful Processor

* kafka-streams kann lokale state-stores verwalten
* diese sind ebenso partitioniert wie das Quell-Topic
* los geht's mit `groupBy` bzw. `groupByKey`
* gruppierter Stream muss dann aggregiert werden
   * count
   * reduce (value -> value -> value)
   * aggregate (aggregate, key -> value -> aggregate -> aggregate)

<!--v-->

### Beispiel

* pro IP-Adresse feststellen wie viele Bytes übertragen wurden

<!--v-->

```java
    @Bean
    public KStream<?, ?> build(StreamsBuilder streamsBuilder) {
        KStream<String, LogEntry> stream = streamsBuilder.stream("streamsdemo.logparser.entries",
            Consumed.with(new StringSerde(), new JsonSerde<>(LogEntry.class)));

        stream
            .mapValues(LogEntry::getBytesSent)
            .groupByKey(Serialized.with(new StringSerde(), new LongSerde()))
            .reduce((a, b) -> a + b)
            .toStream()
            .foreach((k, v) -> log.info("Bytes sent to {} so far: {}", k, v));

        return stream;
    }
```

<!--v-->

## Windowing

* ein gruppierter Stream kann nochmal in Fenster geteilt werden
   * Zeit-basiert (ggf. mit Überlappung)
   * Session-basiert

<!--v-->

## Beispiel

* Auswertung: Anzahl 500er Responses pro 5-Minuten-Fenster?


<!--v-->

## Processor API

* low-level API, bei der man jeden Verarbeitungsknoten selbst "from scratch" erstellt
* process-Methode bekommt einfach jede eingehende Nachricht
* kann beliebig Nachrichten in der Topologie weiterreichen
* beliebige Zugriffe auf State-Store
* punctuation (scheduler-getriggered $dinge tun)

<!--v-->

## Beispiel

* Auswertung: haben wir 401er, auf die kein 200er folgt (innerhalb 10 Sekunden)?

<!--v-->

@todo Beispielcode

