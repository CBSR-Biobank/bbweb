/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: locationsPanelDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q, $rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                  = this.$injector.get('$q');
      self.Centre              = self.$injector.get('Centre');
      self.Location            = self.$injector.get('Location');
      self.modalService        = self.$injector.get('modalService');
      self.domainEntityService = self.$injector.get('domainEntityService');
      self.jsonEntities        = self.$injector.get('jsonEntities');
      self.createEntities      = createEntities;
      self.createController    = createController;

      testUtils.addCustomMatchers();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/locationsPanel/locationsPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');

      function createEntities() {
        var entities = {};

        entities.centre = new self.Centre(self.jsonEntities.centre());
        entities.locations = _.map(_.range(3), function () {
          return new self.Location(self.jsonEntities.location());
        });
        return entities;
      }

      function createController(centre) {
        var element = angular.element([
          '<uib-accordion close-others="false">',
          '  <locations-panel ',
          '    centre="vm.centre"',
          '  </locations-panel>',
          '</uib-accordion>'
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = { centre: centre };
        $compile(element)(self.scope);
        self.scope.$digest();
        self.controller = element.find('locations-panel').controller('locationsPanel');
      }
    }));

    it('has valid scope', function() {
      var entities = this.createEntities();
      this.createController(entities.centre);
      expect(this.controller.centre).toBe(entities.centre);
    });

    it('can add a location', function() {
      var $state = this.$injector.get('$state'),
          entities = this.createEntities();

      this.createController(entities.centre);
      spyOn($state, 'go').and.callFake(function () {});
      this.controller.add();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(
        'home.admin.centres.centre.locations.locationAdd',
        {},
        { reload: true });
    });

    it('can remove a location', function() {
      var entities            = this.createEntities(),
          locationToRemove    = entities.locations[0];

      entities.centre.locations.push(locationToRemove);
      this.createController(entities.centre);

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(this.$q.when(entities.centre));

      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(this.Centre.prototype.removeLocation).toHaveBeenCalled();
    });

    it('displays information modal when removal of a location fails', function() {
      var entities            = this.createEntities(),
          locationToRemove    = entities.locations[0],
          deferred            = this.$q.defer();

      deferred.reject('simulated remove error');
      entities.centre.locations.push(locationToRemove);

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(deferred.promise);

      this.createController(entities.centre);
      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(this.modalService.showModal.calls.count()).toBe(2);
    });

  });

});
