package com.example.actor.repository;

import java.io.Serializable;
import java.util.List;

import com.example.actor.web.ActorForm;

public class TestSessionForms implements Serializable {
	private static final long serialVersionUID = 4674112863194397526L;
	private List<ActorForm> actorForms;

	public List<ActorForm> getActorForms() {
		return actorForms;
	}

	public void setActorForms(List<ActorForm> actorForms) {
		this.actorForms = actorForms;
	}
}