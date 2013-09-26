/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {

	slidePage : function(page) {

		var currentPageDest, self = this;

		// If there is no current page (app just started) -> No transition:
		// Position new page in the view port
		if (!this.currentPage) {
			$(page.el).attr('class', 'page stage-center');
			$('body').append(page.el);
			this.currentPage = page;
			return;
		}

		// Cleaning up: remove old pages that were moved out of the viewport
		$('.stage-right, .stage-left').not('.homePage').remove();

		if (page === app.homePage) {
			// Always apply a Back transition (slide from left) when we go back
			// to the search page
			$(page.el).attr('class', 'page stage-left');
			currentPageDest = "stage-right";
		} else {
			// Forward transition (slide from right)
			$(page.el).attr('class', 'page stage-right');
			currentPageDest = "stage-left";
		}

		$('body').append(page.el);

		// Wait until the new page has been added to the DOM...
		setTimeout(function() {
			// Slide out the current page: If new page slides from the right ->
			// slide current page to the left, and vice versa
			$(self.currentPage.el).attr('class',
					'page transition ' + currentPageDest);
			// Slide in the new page
			$(page.el).attr('class', 'page stage-center transition');
			self.currentPage = page;
		});

	},

	showAlert : function(message, title) {
		if (navigator.notification) {
			navigator.notification.alert(message, null, title, 'OK');
		} else {
			alert(title ? (title + ": " + message) : message);
		}
	},

	registerEvents : function() {
		$(window).on('hashchange', $.proxy(this.route, this));
		$('body').on('mousedown', 'a', function(event) {
			$(event.target).addClass('tappable-active');
		});
		$('body').on('mouseup', 'a', function(event) {
			$(event.target).removeClass('tappable-active');
		});
	},

	route : function() {
		var self = this;
		var hash = window.location.hash;
		if (!hash) {
			if (this.homePage) {
				this.slidePage(this.homePage);
			} else {
				this.homePage = new HomeView(this.store).render();
				this.slidePage(this.homePage);
			}
			return;
		}
		var match = hash.match(this.detailsURL);
		if (match) {
			this.store.findById(Number(match[1]), function(employee) {
				self.slidePage(new EmployeeView(employee).render());
			});
		}
	},
	// Bind Event Listeners
	//
	// Bind any events that are required on startup. Common events are:
	// 'load', 'deviceready', 'offline', and 'online'.
	bindEvents : function() {
		document.addEventListener('deviceready', this.onDeviceReady, false);
	},

	// deviceready Event Handler
	//
	// The scope of 'this' is the event. In order to call the 'receivedEvent'
	// function, we must explicity call 'app.receivedEvent(...);'
	onDeviceReady : function() {
		
		alert("onDeviceReady");
		var pushNotification = window.plugins.pushNotification;
		alert("1");
		pushNotification.register(app.successHandler, app.errorHandler, {
			"senderID" : "346033639851",
			"ecb" : "app.onNotificationGCM"
		});
		alert("2");
		app.receivedEvent('deviceready');
		
		
	},
	// result contains any message sent from the plugin call
	successHandler : function(result) {
		alert('Callback Success! Result = ' + result)
	},
	errorHandler : function(e) {
		alert('Callback error! Result = ' +e);
	},
	// Update DOM on a Received Event
	receivedEvent : function(id) {
		
		var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);

	},
	// Application Constructor
	initialize : function() {

		this.bindEvents();

		var self = this;
		this.detailsURL = /^#employees\/(\d{1,})/;
		this.registerEvents();
		this.store = new MemoryStore(function() {
			self.route();
		});

	},

	onNotificationGCM : function(e) {
		switch (e.event) {
		case 'registered':
			if (e.regid.length > 0) {
				console.log("Regid " + e.regid);
				alert('registration id = ' + e.regid);
			}
			break;

		case 'message':
			// this is the actual push notification. its format depends on the
			// data model from the push server
			alert('message = ' + e.message + ' msgcnt = ' + e.msgcnt);
			break;

		case 'error':
			alert('GCM error = ' + e.msg);
			break;

		default:
			alert('An unknown GCM event has occurred');
			break;
		}
	}
};

app.initialize();
