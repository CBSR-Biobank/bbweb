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
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Directive: locationsPanelDirective', function() {
    var scope, createEntities, createController, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($templateCache, fakeDomainEntities) {
      var templates;

      fakeEntities = fakeDomainEntities;
      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();

      testUtils.putHtmlTemplates($templateCache,
                                 '/assets/javascripts/admin/centres/directives/locationsPanel/locationsPanel.html',
                                 '/assets/javascripts/common/directives/panelButtons.html',
                                 '/assets/javascripts/common/directives/updateRemoveButtons.html');

    }));

    function setupEntities(injector) {
      var Centre = injector.get('Centre'),
          Location = injector.get('Location');

      return create;

      //---

      function create() {
        var entities = {};

        entities.centre = new Centre(fakeEntities.centre());
        entities.locations = _.map(_.range(3), function () {
          return new Location(fakeEntities.location());
        });
        return entities;
      }

    }

    function setupController(injector) {
      var $rootScope = injector.get('$rootScope'),
          $compile   = injector.get('$compile');

      return create;

      //--

      function create(centre) {
        var element = angular.element([
          '<uib-accordion close-others="false">',
          '  <locations-panel centre="vm.centre">',
          '  </locations-panel>',
          '</uib-accordion>'
        ].join(''));

        scope = $rootScope.$new();
        scope.vm = {
          centre: centre
        };
        $compile(element)(scope);
        scope.$digest();
        return element.find('locations-panel').controller('locationsPanel');
      }
    }

    it('has valid scope', function() {
      var entities = createEntities(),
          controller = createController(entities.centre);

      expect(controller.centre).toBe(entities.centre);
    });

    it('can add a location', function() {
      var $state = this.$injector.get('$state'),
          entities = createEntities(),
          controller = createController(entities.centre);

      spyOn($state, 'go').and.callFake(function () {});
      controller.add();
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.admin.centres.centre.locationAdd');
    });

    it('can view location information', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          locationToView = entities.locations[0],
          controller;

      entities.centre.locations.push(locationToView);
      controller = createController(entities.centre);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () {});
      controller.information(locationToView);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can update a location', function() {
      var $state = this.$injector.get('$state'),
          entities = createEntities(),
          locationToUpdate = entities.locations[0],
          controller;

      entities.centre.locations.push(locationToUpdate);
      controller = createController(entities.centre);

      spyOn($state, 'go').and.callFake(function () {});
      controller.update(locationToUpdate);
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.admin.centres.centre.locationUpdate',
                                             { locationId: locationToUpdate.id});
    });

    it('can remove a location', function() {
      var $q                  = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          locationToRemove    = entities.locations[0],
          controller;

      entities.centre.locations.push(locationToRemove);
      controller = createController(entities.centre);

       spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
       });
      spyOn(entities.centre, 'removeLocation').and.callFake(function () {
        return $q.when(entities.centre);
      });
      controller.remove(locationToRemove);
      scope.$digest();
      expect(entities.centre.removeLocation).toHaveBeenCalledWith(locationToRemove);
    });

    it('displays information modal when removal of a location fails', function() {
      var $q                  = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          locationToRemove    = entities.locations[0],
          controller;

      entities.centre.locations.push(locationToRemove);
      controller = createController(entities.centre);

       spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
       });
      spyOn(entities.centre, 'removeLocation').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      controller.remove(locationToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});
