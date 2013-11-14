package com.ambiata.saws
package iam


/** IAM policy. */
case class Policy(name: String, document: String)


/** Constructors for different policies. */
object Policy {

  /** Create a policy allowing 'GetObject' and 'ListBucket' for the specified S3 path. */
  def allowS3ReadPath(path: String): Policy = {
    val name = s"ReadAccessTo_$path".replace('/', '+')
    Policy(name, allowS3PathForActions(path, Seq("GetObject")))
  }

  /** Create a policy allowing 'PutObject' and 'ListBucket' for the specified S3 path. */
  def allowS3WritePath(path: String): Policy  = {
    val name = s"WriteAccessTo_$path".replace('/', '+')
    Policy(name, allowS3PathForActions(path, Seq("PutObject")))
  }

  /** Create a policy allowing 'PutObject', 'GetObject' and 'ListBucket' for the specified S3 path. */
  def allowS3ReadWritePath(path: String): Policy  = {
    val name = s"ReadWriteAccessTo_$path".replace('/', '+')
    Policy(name, allowS3PathForActions(path, Seq("PutObject", "GetObject")))
  }

  /** Create a policy allowing 'ListBucket' and other S3 actions for the specified S3 path. */
  def allowS3PathForActions(path: String, actions: Seq[String]) = {
    val s3Actions = actions.map(a => s""""s3:${a}"""").mkString(",")
    val bucket = path.takeWhile(_ != '/')
    s"""|{
        |  "Version": "2012-10-17",
        |  "Statement": [
        |    {
        |      "Action": [ ${s3Actions} ],
        |      "Resource": [ "arn:aws:s3:::$path/*" ],
        |      "Effect": "Allow"
        |    },
        |    {
        |      "Action": [ "s3:ListBucket" ],
        |      "Resource": [ "arn:aws:s3:::$bucket" ],
        |      "Effect": "Allow"
        |    }
        |  ]
        |}""".stripMargin
  }

  /** Create a policy allowing full access to all EC2 actions. */
  val allowEc2FullAccess: Policy = {
    val doc =
      s"""|{
          |  "Version": "2012-10-17",
          |  "Statement": [
          |    {
          |      "Action": "ec2:*",
          |      "Effect": "Allow",
          |      "Resource": "*"
          |    }
          |  ]
          |}""".stripMargin
    Policy("ec2-full-access", doc)
  }

  /** Create a policy allowing access to describe tags. */
  val allowEc2DescribeTags: Policy = {
    val doc =
      s"""|{
          |  "Version": "2012-10-17",
          |  "Statement": [
          |    {
          |      "Agtion": "ec2:DescribeTags",
          |      "Effect": "Allow",
          |      "Resource": "*"
          |    }
          |  ]
          |}""".stripMargin
    Policy("ec2-describe-tags", doc)
  }

  /** Create a policies for allowing full access to all EMR actions as well as read-only access
    * to the 'elasticmapreduce' S3 buckets (for the purpose of running standard EMR bootstrap
    * actions and steps). */
  val allowEmrFullAccess: List[Policy] = {
    val doc =
      s"""|{
          |  "Version": "2012-10-17",
          |  "Statement": [
          |    {
          |      "Action": [
          |        "elasticmapreduce:*",
          |        "ec2:AuthorizeSecurityGroupIngress",
          |        "ec2:CancelSpotInstanceRequests",
          |        "ec2:CreateSecurityGroup",
          |        "ec2:CreateTags",
          |        "ec2:DescribeAvailabilityZones",
          |        "ec2:DescribeInstances",
          |        "ec2:DescribeKeyPairs",
          |        "ec2:DescribeRouteTables",
          |        "ec2:DescribeSecurityGroups",
          |        "ec2:DescribeSpotInstanceRequests",
          |        "ec2:DescribeSubnets",
          |        "ec2:ModifyImageAttribute",
          |        "ec2:ModifyInstanceAttribute",
          |        "ec2:RequestSpotInstances",
          |        "ec2:RunInstances",
          |        "ec2:TerminateInstances",
          |        "cloudwatch:*",
          |        "sdb:*"
          |      ],
          |      "Effect": "Allow",
          |      "Resource": "*"
          |    }
          |  ]
          |}""".stripMargin
    List(
      Policy("emr-full-access", doc),
      allowS3ReadPath("elasticmapreduce"),
      allowS3ReadPath("ap-southeast-2.elasticmapreduce")
    )
  }
}
