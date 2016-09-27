package com.jobaline.uiautomation.framework.core.actor;

import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by damian on 4/16/15.
 */
public class TestsActorSystem
{
	private static ActorSystem actorSystem;

	private static Map<String, Object> singletonActors = new HashMap<>();


	public static synchronized ActorSystem getActorSystem()
	{
		if(actorSystem == null)
		{
			Config defaultConfig = ConfigFactory.load();

			String config_json = ""
				+ "{"
				+ "	\"akka\": {"
				+ "		\"actor\" {"
				+ "			\"guardian-supervisor-strategy\" : \"com.jobaline.uiautomation.framework.core.actor.DefaultGuardianSupervisorStrategy\","
				+ "			\"default-dispatcher\" {"
				+ "				\"throughput\": 1,"
				+ "				\"fork-join-executor\": {"
				+ "					\"parallelism-factor\": 10,"
				+ "					\"parallelism-max\": 64,"
				+ "					\"parallelism-min\": 8"
				+ "				}"
				+ "			}"
				+ "		}"
				+ "  }"
				+ "}";

			Config config = ConfigFactory.parseString(config_json).withFallback(defaultConfig);

			actorSystem = ActorSystem.create("TestsActorSystem", config);
		}

		return actorSystem;
	}


	public static <T1, T2 extends T1> T1 getActor(Class<T2> clazz, long timeout)
	{
		TypedProps<T2> props = new TypedProps<>(clazz).withTimeout(Timeout.longToTimeout(timeout));

		return TypedActor.get(getActorSystem()).typedActorOf(props);
	}


	/**
	 * Will use the class as the actor id
	 * */
	public synchronized static <T1, T2 extends T1> T1 getSingletonActor(Class<T2> clazz, long timeout)
	{
		return getSingletonActor(clazz, null, timeout);
	}


	/**
	 * Will use the parameters clazz and secondaryId to identify the actor. If secondaryId is null, will use only the clazz parameter.
	 * */
	public synchronized static <T1, T2 extends T1> T1 getSingletonActor(Class<T2> clazz, String secondaryId, long timeout)
	{
		String actorId = secondaryId != null? clazz.getName() + secondaryId : clazz.getName();

		if(!singletonActors.containsKey(actorId))
		{
			singletonActors.put(actorId, getActor(clazz, timeout));
		}

		return (T1)singletonActors.get(actorId);
	}

}
