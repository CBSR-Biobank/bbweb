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

  describe('Controller: SpecimenGroupEditCtrl', function() {
    var createEntities,
        createController,
        AnatomicalSourceType,
        PreservationType,
        PreservationTemperatureType,
        SpecimenType;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($injector) {
      createEntities   = setupEntities($injector);
      createController = setupController($injector);

      AnatomicalSourceType        = $injector.get('AnatomicalSourceType');
      PreservationType            = $injector.get('PreservationType');
      PreservationTemperatureType = $injector.get('PreservationTemperatureType');
      SpecimenType                = $injector.get('SpecimenType');

      testUtils.addCustomMatchers();
    }));

    function setupEntities(injector) {
      var Study         = injector.get('Study'),
          SpecimenGroup = injector.get('SpecimenGroup'),
          fakeEntities  = injector.get('fakeDomainEntities');

      return create;

      //--

      function create(options) {
        var study, specimenGroup;

        options = options || {};

        study         = new Study(fakeEntities.study());

        if (options.noSgId) {
          specimenGroup = new SpecimenGroup(_.omit(fakeEntities.processingType(study), 'id'));
        } else {
          specimenGroup = new SpecimenGroup(fakeEntities.processingType(study));
        }

        return {
          study:         study,
          specimenGroup: specimenGroup
        };
      }
    }

    function setupController(injector) {
      var rootScope                   = injector.get('$rootScope'),
          controller                  = injector.get('$controller'),
          state                       = injector.get('$state'),
          domainEntityService         = injector.get('domainEntityService'),
          notificationsService        = injector.get('notificationsService');

      return create;

      //--

      function create(entities) {
        var scope = rootScope.$new();

        controller('SpecimenGroupEditCtrl as vm', {
          $scope:                      scope,
          $state:                      state,
          domainEntityService:         domainEntityService,
          notificationsService:        notificationsService,
          AnatomicalSourceType:        AnatomicalSourceType,
          PreservationType:            PreservationType,
          PreservationTemperatureType: PreservationTemperatureType,
          SpecimenType:                SpecimenType,
          study:                       entities.study,
          specimenGroup:               entities.specimenGroup
        });

        scope.$digest();
        return scope;
      }
    }

    describe('has valid scope when created', function () {

      it('for new specimen group', function() {
        var entities = createEntities({ noSgId: true }),
            scope = createController(entities);

        expect(scope.vm.title).toBe('Add Specimen Group');
        initScopeCommon(entities, scope);
      });

      it('for existing specimen group', function() {
        var entities = createEntities(),
            scope = createController(entities);
        expect(scope.vm.title).toBe('Update Specimen Group');
        initScopeCommon(entities, scope);
      });

      function initScopeCommon(entities, scope) {
        expect(scope.vm.study).toBe(entities.study);
        expect(scope.vm.specimenGroup).toBe(entities.specimenGroup);
        expect(scope.vm.anatomicalSourceTypes ).toBe(AnatomicalSourceType.values());
        expect(scope.vm.preservTypes).toBe(PreservationType.values());
        expect(scope.vm.preservTempTypes).toBe(PreservationTemperatureType.values());
        expect(scope.vm.specimenTypes).toBe(SpecimenType.values());
      }

    });

    it('can submit a specimen group', function() {
      var q        = this.$injector.get('$q'),
          state    = this.$injector.get('$state'),
          entities = createEntities(),
          scope    = createController(entities);

      spyOn(entities.specimenGroup, 'addOrUpdate').and.callFake(function () {
        return q.when(entities.specimenGroup);
      });
      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.submit(entities.specimenGroup);
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.specimens', {}, {reload: true});
    });

    it('on submit error, displays an error modal', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          scope               = createController(entities);

      spyOn(entities.specimenGroup, 'addOrUpdate').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('xxx');
        return deferred.promise;
      });
      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});

      scope.vm.submit(entities.specimenGroup);
      scope.$digest();

      expect(domainEntityService.updateErrorModal)
        .toHaveBeenCalledWith('xxx', 'specimen group');
    });

    it('when user presses the cancel button, goes to correct state', function() {
      var state    = this.$injector.get('$state'),
          entities = createEntities(),
          scope    = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.cancel();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.specimens', {}, {reload: true});
    });

  });

});
