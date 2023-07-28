# LogParser
__Hi guys!__

It's me, Roman Liashenko, and this is my __LogParser__!

Imagine the situation — you are a developer, and your application has logger which logs literally everything during all the time it works. 
And when you decide to check did something bad (like ERRORs) happened or not, it's pretty hard and time-consuming to search from all the log files...

That's exectly why I created this __LogParser__!

It finds a __.log__ file in the package __logs__ (it's important to have only one .log file in this package, otherwise program will read only the first one), 
read it, parse every line of logs and storage them in the List of _LogEntries_ for comfortable work with all the collected data. 
This program is tailored to a specific log template, but it can also be modified to suit your needs. And then, when everything is ready, you can use all its various
methods (most of them works using ___Stream API___) to sort and get messages you want! You can use those methods directly via LogParser instance, or using it's special method __execute()__, 
which takes one String argument as a query and returns _Set\<Object\>_ with requested data. It uses __RegEx__ to parse the requests.

This method's argument must have a certain structure:

___get param1 for param2 = "value"___,

where _param1_ — one of the fields: ip, user, date, event or status;

_param2_ — one of the fields: ip, user, date, event or status;

_value_ — value of the field _param2_.

Examples: 

1\) get ip for user = "Vasya" (returns set of all the IPs of this user)

2\) get user for event = "DONE_TASK" (returns set of all the users with this event)

3\) get event for date = "03.01.2014 03:45:23" (return set of all the events happened in a certain date)


Also there is extended version of structure of the argument:

___get field1 for field2 = "value1" and date between "after" and "before"___,

where _after_ — the lower threshold of the _date_ interval, a date in format of "dd.MM.yyyy HH:mm:ss"

_before_ — upper threshold of the _date_ interval, a date in format of "dd.MM.yyyy HH:mm:ss"

It privides you to do the same request as previous, but with _additional_ filter by the certain date interval.

Example:

get ip for user = "Eduard Petrovich Morozko" and date between "11.12.2013 0:00:00" and "03.01.2014 23:59:59".

Note, that Event and Status are Enums, declared in corresponding files.

That's all for now! If you have any questions, feel free to ask me in DM! See ya =)
