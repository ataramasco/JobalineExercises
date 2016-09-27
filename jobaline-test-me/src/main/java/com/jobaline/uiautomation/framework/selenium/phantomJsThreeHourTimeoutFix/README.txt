Selenium is using http as the transport for sending commands to the WebDriver driver in the browser. The class
org.openqa.selenium.remote.internal.HttpClientFactory has a hardcoded timeout of 3 hours for the http
sockets. When phantomjs becomes unresponsive, it does not send any data back to the socket but it does
not either close the connection, so the operation will timeout in 3 hours. I didn't see it happening in other
browsers. When they become unresponsive, the WebDriver Session is finished (either by the WebDriver driver
in the browser o by a proxy when we use services as SauceLabs or Browserstacks).

So I had to implement a patch. As the harcoded timeout is in a private variable, I couldn't just extend so I
copied and edited the class that would take less job (HttpCommandExecutor) but that made me to copy
other classes since they have a package scope.

In newer versions of Selenium, it seems that there is a way to set a value for this timeout, but we can't update
selenium since the latest version of GhostDriver (1.2) requires selenium 2.41 (even when we add a newer selenium-java
dependency, some jars will be override by GhostDriver dependencies).

Another option would be to update selenium to its latest version, fork GhostDriver and update its classes (since it only has
3 classes!) but I tried to keep everything the closest to its original state.

Use this patch only for PhantomJS through the class PhantomJSDriver located in this folder, do NEVER use this classes
for another browsers since they don't have this issue. Also, timeouts were adjusted specifically to phantomjs.

The only difference in client code is to instantiate this PhantomJSDriver class instead of the one coming from the
official library. But there is no other action required. At any time you can instantiate PhantomJSDriver using the
official library version, remove all these classes and everything is going to be fine (except that you will
still suffer the 3 hour timeout).
