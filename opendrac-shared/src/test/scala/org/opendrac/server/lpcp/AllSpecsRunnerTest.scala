package org.opendrac.server.lpcp

import org.specs.runner.{JUnit4, ConsoleRunner}

class AllSpecsRunnerTest extends JUnit4(OnePlusOnePathUtilSpec)

object AllSpecsRunner extends ConsoleRunner(OnePlusOnePathUtilSpec)
