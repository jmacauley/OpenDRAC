package org.opendrac.server.lpcp

import org.specs.mock.Mockito
import org.specs.Specification
import java.io.ObjectInputStream
import java.util.{ArrayList, Map, List}
import com.nortel.appcore.app.drac.common.types.{CrossConnection, SPF_KEYS}

//import collection.Javaonversions._


object OnePlusOnePathUtilSpec extends Specification with Mockito {

  private val onePlusOnePathUtil = new OnePlusOnePathUtil
  private val srcNeid = "00-20-D8-DF-33-8B"
  private val dstNeid = "00-20-D8-DF-33-59"
  private val workingPaths = new ObjectInputStream(this.getClass.getResourceAsStream("/one_plus_one/data/workingPaths.ser")).readObject.asInstanceOf[List[Map[SPF_KEYS, Object]]]
  private val protectionPaths = new ObjectInputStream(this.getClass().getResourceAsStream("/one_plus_one/data/protectionPaths.ser")).readObject.asInstanceOf[List[Map[SPF_KEYS, Object]]]
  private val originalResult = new ObjectInputStream(this.getClass().getResourceAsStream("/one_plus_one/data/org_result.ser")).readObject.asInstanceOf[List[CrossConnection]]

  private val reallyVerbose = true


  "A OnePlusOnePathUtil" should {
    doFirst {

      // setSequential
      // shareVariables

      if (reallyVerbose) {
        println("Printing: protectionPaths")
        onePlusOnePathUtil.printConnectionDetails(protectionPaths)

        println("Printing: workingPaths")
        onePlusOnePathUtil.printConnectionDetails(workingPaths)

        println("Printing: originalResult: " + originalResult)

      }

    }

    "throw a NullPointerException when calling crossConnectionsSummaryAsString with a null argument" in {
      onePlusOnePathUtil.crossConnectionsSummaryAsString(null) must throwA(new NullPointerException("cross connections can not be null"))
    }

    "throw a NullPointerException with RoutingException details when calling get1Plus1Path with all null arguments" in {
      onePlusOnePathUtil.get1Plus1Path(null, null, null, null) must throwA(new NullPointerException("srcNeid can not be null [com.nortel.appcore.app.drac.common.errorhandling.RoutingException: Unexpected error calculating 1+1 path]"))
    }

    "throw a IllegalArgumentException when calling get1Plus1Path with empty collection" in {
      onePlusOnePathUtil.get1Plus1Path(srcNeid, dstNeid, new ArrayList[Map[SPF_KEYS, Object]], new ArrayList[Map[SPF_KEYS, Object]]) must throwA(new IllegalArgumentException("workingPaths must contain one element"))
    }

    "return 30 crossconnections from working and protected paths when calling get1Plus1Path" in {
      warning("Currently returns the wrong amount of merged xconnects")
      onePlusOnePathUtil.get1Plus1Path(srcNeid, dstNeid, workingPaths, protectionPaths).size mustBe 30
    }

    "return 18 crossconnections from merging working and protected paths when calling get1Plus1Path" in {
      skip("To be implemented")
      onePlusOnePathUtil.get1Plus1Path(srcNeid, dstNeid, workingPaths, protectionPaths).size mustBe 18
    }


    "not return a swmate channel which grew in steps other then 3" in {}

    "never return a swmate of type ethernet" in {}

    "for each Xconnect with MAC = dstNode or strNode, merge Xconnect working and protection" in {}

    "for each Xconnect with MAC!= dstNode or strNode of protection and working must be included end result" in {}

    "end result: number of Xconnects at each node should equal number of timslots." in {}

    "total number of Xconnects: number of nodes * Timeslots" in {}

    "return a strNode with a eth port at Target" in {}

    "return a dstnode with a eth port at Target" in {}

    "return a nicely formatted String with the CrossConnections to be used when calling crossConnectionsSummaryAsString" in {
      warning("Still have to really check the end result")
      val crossconnections = onePlusOnePathUtil.get1Plus1Path(srcNeid, dstNeid, workingPaths, protectionPaths)
      val stringResult = onePlusOnePathUtil.crossConnectionsSummaryAsString(crossconnections)
      stringResult mustNotBe null
      // TODO: Really check the result
      if (reallyVerbose) println(stringResult)
    }

    "print original result" in {
      val a = onePlusOnePathUtil.crossConnectionsSummaryAsString(originalResult)
      println("Original: " + a)
      a mustNotBe null
    }


  }

}