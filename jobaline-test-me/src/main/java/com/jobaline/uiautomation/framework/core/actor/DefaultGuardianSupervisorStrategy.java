package com.jobaline.uiautomation.framework.core.actor;

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategyConfigurator;
import akka.japi.Function;
import com.jobaline.uiautomation.framework.core.orchestrator.CouldNotResolveEntityDependencyException;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation.CouldNotCreateEntityException;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by damian on 7/16/15.
 */
public class DefaultGuardianSupervisorStrategy
	implements SupervisorStrategyConfigurator
{

	@Override public SupervisorStrategy create()
	{
		return new OneForOneStrategy(5, Duration.create(100, TimeUnit.MILLISECONDS),
			new Function<Throwable, SupervisorStrategy.Directive>()
			{
				@Override
				public SupervisorStrategy.Directive apply(Throwable t)
				{
					if(t instanceof CouldNotCreateEntityException || t instanceof CouldNotResolveEntityDependencyException)
					{
						return SupervisorStrategy.resume();
					}
					else
					{
						// If it is not an known exception, will return the akka default
						return SupervisorStrategy.restart();
					}
				}
			});
	}

}
