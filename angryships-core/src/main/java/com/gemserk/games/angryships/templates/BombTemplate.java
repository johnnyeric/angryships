package com.gemserk.games.angryships.templates;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.MovementComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Contacts;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.games.angryships.components.ControllerComponent;
import com.gemserk.games.angryships.components.ExplosionComponent;
import com.gemserk.games.angryships.components.GameComponents;
import com.gemserk.games.angryships.components.PixmapCollidableComponent;
import com.gemserk.games.angryships.components.PixmapCollision;
import com.gemserk.games.angryships.entities.Collisions;
import com.gemserk.games.angryships.entities.Events;
import com.gemserk.games.angryships.entities.Groups;
import com.gemserk.games.angryships.gamestates.Controller;
import com.gemserk.games.angryships.resources.GameResources;
import com.gemserk.games.angryships.scripts.MovementScript;
import com.gemserk.resources.ResourceManager;

public class BombTemplate extends EntityTemplateImpl {

	public static class PixmapCollidableScript extends ScriptJavaImpl {

		EventManager eventManager;

		@Override
		public void update(World world, Entity e) {
			PixmapCollidableComponent pixmapCollidableComponent = GameComponents.getPixmapCollidableComponent(e);
			PixmapCollision pixmapCollision = pixmapCollidableComponent.pixmapCollision;

			if (!pixmapCollision.isInContact())
				return;

			e.delete();

			eventManager.registerEvent(Events.explosion, e);
		}

	}
	
	public static class ExplodeWhenCollisionScript extends ScriptJavaImpl {
		
		EventManager eventManager;
		
		@Override
		public void update(World world, Entity e) {
			
			PhysicsComponent physicsComponent = Components.physicsComponent(e);
			Contacts contacts = physicsComponent.getContact();
			
			if (!contacts.isInContact())
				return;
			
			e.delete();
			eventManager.registerEvent(Events.explosion, e);
			
		}
		
	}

	ResourceManager<String> resourceManager;
	Injector injector;
	BodyBuilder bodyBuilder;

	@Override
	public void apply(Entity entity) {

		Spatial spatial = parameters.get("spatial");
		Controller controller = parameters.get("controller");

		Sprite sprite = resourceManager.getResourceValue(GameResources.Sprites.BombSprite);

		entity.setGroup(Groups.Bombs);

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(10f) //
						.categoryBits(Collisions.Bomb) //
						.maskBits((short)(Collisions.Target | Collisions.Explosion)) //
						.sensor() //
				) //
				.position(spatial.getX(), spatial.getY()) //
				.angle(0f) //
				.userData(entity) //
				.type(BodyType.DynamicBody) //
				.build();

		entity.addComponent(new PhysicsComponent(body));

		entity.addComponent(new RenderableComponent(0));
		entity.addComponent(new SpriteComponent(sprite, 0.5f, 0.5f, Color.WHITE));

		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, spatial)));
		// entity.addComponent(new PreviousStateSpatialComponent());

		entity.addComponent(new MovementComponent(150f, 0f, 0f));

		entity.addComponent(new ControllerComponent(controller));

		entity.addComponent(new PixmapCollidableComponent());
		entity.addComponent(new ExplosionComponent(injector.getInstance(ExplosionAnimationTemplate.class), 32f));

		Script movementScript = injector.getInstance(MovementScript.class);
		Script pixmapCollidableScript = injector.getInstance(PixmapCollidableScript.class);

		entity.addComponent(new ScriptComponent(movementScript, pixmapCollidableScript, injector.getInstance(ExplodeWhenCollisionScript.class)));

	}

}
