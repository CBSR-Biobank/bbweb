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
      _                             = require('lodash'),
      faker                         = require('faker'),
      locationUpdateSharedBehaviour = require('../../../../test/locationUpdateSharedBehaviourSpec');

  describe('Directive: centreLocationViewDirective', function() {

    var createController = function (centre, location) {
      centre = centre || this.centre;
      location = location || this.location;

      expect(_.find(centre.locations, { uniqueId: location.uniqueId})).toBeDefined();

      this.element = angular.element(
        '<centre-location-view centre="vm.centre" location="vm.location"></centre-location-view>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        centre: centre,
        location: location
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centreLocationView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($state, TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Location',
                              'factory');

      self.location = new self.Location(self.factory.location());
      self.centre = new self.Centre(self.factory.centre({ locations: [ self.location ]}));

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreLocationView/centreLocationView.html');

      self.returnStateName = 'home.admin.centres.centre.locations';
    }));

    it('scope should be valid', function() {
      createController.call(this);
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
      createController.call(this);
      spyOn(this.$state, 'go').and.returnValue(null);
      this.controller.back();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName, {}, { reload: true });
    });

    describe('updates to location', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity                   = this.Centre;
        context.entityUpdateFuncName     = 'updateLocation';
        context.createController         = createController;
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
