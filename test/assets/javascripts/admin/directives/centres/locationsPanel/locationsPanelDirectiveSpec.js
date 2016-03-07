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

    beforeEach(inject(function($q, testUtils) {
      var self = this;

      self.jsonEntities = self.$injector.get('jsonEntities');
      self.createEntities = setupEntities();
      self.createController = setupController();
      testUtils.addCustomMatchers();

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/locationsPanel/locationsPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');

      self.onRemove = jasmine.createSpy('onRemove').and.returnValue($q.when(true));

      function setupEntities() {
        var Centre = self.$injector.get('Centre'),
            Location = self.$injector.get('Location');

        return create;

        //---

        function create() {
          var entities = {};

          entities.centre = new Centre(self.jsonEntities.centre());
          entities.locations = _.map(_.range(3), function () {
            return new Location(self.jsonEntities.location());
          });
          return entities;
        }
      }

      function setupController() {
        var $rootScope = self.$injector.get('$rootScope'),
            $compile   = self.$injector.get('$compile');

        return create;

        //--

        function create(centre) {
          var element = angular.element([
            '<uib-accordion close-others="false">',
            '  <locations-panel ',
            '    centre="vm.centre"',
            '    on-remove="vm.onRemove">',
            '  </locations-panel>',
            '</uib-accordion>'
          ].join(''));

          self.scope = $rootScope.$new();
          self.scope.vm = {
            centre: centre,
            onRemove: self.onRemove
          };
          $compile(element)(self.scope);
          self.scope.$digest();
          self.controller = element.find('locations-panel').controller('locationsPanel');
        }
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
      expect($state.go).toHaveBeenCalledWith('home.admin.centres.centre.locationAdd');
    });

    it('can view location information', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = this.createEntities(),
          locationToView = entities.locations[0];

      entities.centre.locations.push(locationToView);
      this.createController(entities.centre);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () {});
      this.controller.information(locationToView);
      this.scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can update a location', function() {
      var $state = this.$injector.get('$state'),
          entities = this.createEntities(),
          locationToUpdate = entities.locations[0];

      entities.centre.locations.push(locationToUpdate);
      this.createController(entities.centre);

      spyOn($state, 'go').and.callFake(function () {});
      this.controller.update(locationToUpdate);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.admin.centres.centre.locationUpdate',
                                             { locationId: locationToUpdate.id});
    });

    it('can remove a location', function() {
      var $q                  = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = this.createEntities(),
          locationToRemove    = entities.locations[0];

      entities.centre.locations.push(locationToRemove);
      this.createController(entities.centre);

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });
      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(this.onRemove).toHaveBeenCalled();
    });

    it('displays information modal when removal of a location fails', function() {
      var $q                  = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = this.createEntities(),
          locationToRemove    = entities.locations[0];

      entities.centre.locations.push(locationToRemove);

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });
      this.onRemove = jasmine.createSpy('onRemove').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      this.createController(entities.centre);
      this.controller.remove(locationToRemove);
      this.scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});
