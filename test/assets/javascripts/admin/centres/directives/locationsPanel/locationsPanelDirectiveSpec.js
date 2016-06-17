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

    var createEntities = function () {
      var self = this, entities = {};

      entities.centre = new self.Centre(self.factory.centre());
      entities.locations = _.map(_.range(3), function () {
        return new self.Location(self.factory.location());
      });
      return entities;
    };

    var createController = function (centre) {
      var element = angular.element('<locations-panel centre="vm.centre"></locations-panel>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { centre: centre };

      this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
      this.scope.$on('centre-view', this.eventRxFunc);

      this.$compile(element)(this.scope);
      this.scope.$digest();
      this.controller = element.controller('locationsPanel');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Centre',
                              'Location',
                              'modalService',
                              'domainEntityService',
                              'factory');

      testUtils.addCustomMatchers();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/locationsPanel/locationsPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');
    }));

    it('initialization is valid', function() {
      var entities = createEntities.call(this);
      createController.call(this, entities.centre);
      expect(this.controller.centre).toBe(entities.centre);
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

    it('can add a location', function() {
      var $state = this.$injector.get('$state'),
          entities = createEntities.call(this);

      createController.call(this, entities.centre);
      spyOn($state, 'go').and.callFake(function () {});
      this.controller.add();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(
        'home.admin.centres.centre.locations.locationAdd',
        {},
        { reload: true });
    });

    it('can remove a location', function() {
      var entities            = createEntities.call(this),
          locationToRemove    = entities.locations[0];

      entities.centre.locations.push(locationToRemove);
      createController.call(this, entities.centre);

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(this.$q.when(entities.centre));

      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(this.Centre.prototype.removeLocation).toHaveBeenCalled();
    });

    it('displays information modal when removal of a location fails', function() {
      var entities            = createEntities.call(this),
          locationToRemove    = entities.locations[0],
          deferred            = this.$q.defer();

      deferred.reject('simulated remove error');
      entities.centre.locations.push(locationToRemove);

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(deferred.promise);

      createController.call(this, entities.centre);
      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(this.modalService.showModal.calls.count()).toBe(2);
    });

  });

});
