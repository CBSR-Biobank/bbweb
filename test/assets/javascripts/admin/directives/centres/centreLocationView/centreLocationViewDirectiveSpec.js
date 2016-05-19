/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular                       = require('angular'),
      mocks                         = require('angularMocks'),
      _                             = require('underscore'),
      faker                         = require('faker'),
      locationUpdateSharedBehaviour = require('../../../../test/locationUpdateSharedBehaviourSpec');

  fdescribe('Directive: centreLocationViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, $state, directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$state               = self.$injector.get('$state');
      self.Centre               = self.$injector.get('Centre');
      self.Location             = self.$injector.get('Location');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      self.location = new self.Location(self.jsonEntities.location());
      self.centre = new self.Centre(self.jsonEntities.centre({ locations: [ self.location ]}));

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreLocationView/centreLocationView.html');

      self.returnStateName = 'home.admin.centres.centre.locations';
      self.createController = createController;

      //--

      function createController(centre, location) {
        centre = centre || self.centre;
        location = location || self.location;

        expect(_.findWhere(centre.locations, { uniqueId: location.uniqueId})).toBeDefined();

        self.element = angular.element(
          '<centre-location-view centre="vm.centre" location="vm.location"></centre-location-view>');
        self.scope = $rootScope.$new();
        self.scope.vm = {
          centre: centre,
          location: location
        };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('centreLocationView');
      }

    }));

    it('scope should be valid', function() {
      this.createController();
      expect(this.controller.centre).toBe(this.centre);
      expect(this.controller.location).toBe(this.location);
      expect(this.controller.back).toBeFunction();
      expect(this.controller.editName).toBeFunction();
      expect(this.controller.editStreet).toBeFunction();
      expect(this.controller.editCity).toBeFunction();
      expect(this.controller.editProvince).toBeFunction();
      expect(this.controller.editPostalCode).toBeFunction();
      expect(this.controller.editPoBoxNumber).toBeFunction();
      expect(this.controller.editCountryIsoCode).toBeFunction();

    });

    it('should return to valid state when back is called', function() {
      this.createController();
      spyOn(this.$state, 'go').and.returnValue(null);
      this.controller.back();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName, {}, { reload: true });
    });

    describe('updates to location', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity                   = this.Centre;
        context.entityUpdateFuncName     = 'updateLocation';
        context.createController         = this.createController;
        context.location                 = this.centre.locations[0];
      }));

      describe('updates to location name', function () {

        beforeEach(inject(function () {
          context.controllerUpdateFuncName = 'editName';
          context.modalInputFuncName       = 'text';
          context.newValue                 = faker.random.word();
        }));

        locationUpdateSharedBehaviour(context);

      });

      describe('updates to location street', function () {

        beforeEach(inject(function () {
          context.controllerUpdateFuncName = 'editStreet';
          context.modalInputFuncName       = 'text';
          context.newValue                 = faker.random.word();
        }));

        locationUpdateSharedBehaviour(context);

      });

      describe('updates to location city', function () {

        beforeEach(inject(function () {
          context.controllerUpdateFuncName = 'editCity';
          context.modalInputFuncName       = 'text';
          context.newValue                 = faker.random.word();
        }));

        locationUpdateSharedBehaviour(context);

      });

      describe('updates to location province', function () {

        beforeEach(inject(function () {
          context.controllerUpdateFuncName = 'editProvince';
          context.modalInputFuncName       = 'text';
          context.newValue                 = faker.random.word();
        }));

        locationUpdateSharedBehaviour(context);

      });

      describe('updates to location postal code', function () {

        beforeEach(inject(function () {
          context.controllerUpdateFuncName = 'editPostalCode';
          context.modalInputFuncName       = 'text';
          context.newValue                 = faker.random.word();
        }));

        locationUpdateSharedBehaviour(context);

      });

      describe('updates to location PO box number', function () {

        beforeEach(inject(function () {
          context.controllerUpdateFuncName = 'editPoBoxNumber';
          context.modalInputFuncName       = 'text';
          context.newValue                 = faker.random.word();
        }));

        locationUpdateSharedBehaviour(context);

      });

      describe('updates to location country ISO code', function () {

        beforeEach(inject(function () {
          context.controllerUpdateFuncName = 'editCountryIsoCode';
          context.modalInputFuncName       = 'text';
          context.newValue                 = faker.random.word();
        }));

        locationUpdateSharedBehaviour(context);

      });

    });


  });

});
