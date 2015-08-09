import java.util.regex.Matcher

String input = """\
commit b0b919a7c42930fa715fc39d37fd2b360bc86551
Merge: 8881d85 6cd1f07
Author: Yunsheng Cao <Yunsheng.Cao@turn.com>
Date:   Mon Jul 20 17:19:53 2015 -0700

    Automatic merge from release/150722 -> master

    * commit '6cd1f07267d5befb649d2606ca51bc642604fefb':
      Add health check for GRP MR job
      Clear comments
      Fix GRP TAG job

12\t2\tsrc/java/com/turn/platform/cheetah/service/CheetahService.java
15\t0\tsrc/java/com/turn/platform/storyteller/forecaster/sampling/stratified/ForecastSampleHealthChecker.java
22\t82\tsrc/java/com/turn/platform/storyteller/forecaster/sampling/stratified/GRPSegmentTagDriver.java
"""

Matcher matcher = input =~ /(?m)^(\d+)\s(\d+)\s(.*)/
assert matcher
println matcher.size()
//matcher.size().times {println matcher[it]}
matcher.each {println it}