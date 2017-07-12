/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Directive: locationsPanelDirective', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createScope = function (scopeVars) {
        var scope = ComponentTestSuiteMixin.prototype.createScope.call(this, scopeVars);
        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        scope.$on('tabbed-page-update', this.eventRxFunc);
        return scope;
      };

      SuiteMixin.prototype.createController = function (centre) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<locations-panel centre="vm.centre"></locations-panel>',
          { centre: centre },
        'locationsPanel');
      };

      SuiteMixin.prototype.createEntities = function () {
        var self = this, entities = {};

        entities.centre = new self.Centre(self.factory.centre());
        entities.locations = _.map(_.range(3), function () {
          return new self.Location(self.factory.location());
        });
        return entities;
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin, testUtils) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Centre',
                              'Location',
                              'modalService',
                              'domainNotificationService',
                              'factory');

      testUtils.addCustomMatchers();

      this.putHtmlTemplates(
        '/assets/javascripts/admin/centres/components/locationsPanel/locationsPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');
    }));

    it('initialization is valid', function() {
      var entities = this.createEntities();
      this.createController(entities.centre);
      expect(this.controller.centre).toBe(entities.centre);
      expect(this.eventRxFunc).toHaveBeenCalled();
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

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(this.$q.when(entities.centre));

      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(this.Centre.prototype.removeLocation).toHaveBeenCalled();
    });

    it('displays information modal when removal of a location fails', function() {
      var entities         = this.createEntities(),
          locationToRemove = entities.locations[0];

      entities.centre.locations.push(locationToRemove);
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));

      this.createController(entities.centre);
      spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(this.$q.reject('simulated error'));
      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(this.modalService.modalOkCancel.calls.count()).toBe(2);
    });

    it('allows a user to view a location', function() {
      var centre = new this.Centre(this.factory.centre()),
          location = new this.Location(this.factory.location());

      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController(centre);
      this.controller.view(location);
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.centres.centre.locations.locationView',
        { locationId: location.id });
    });


  });

});
