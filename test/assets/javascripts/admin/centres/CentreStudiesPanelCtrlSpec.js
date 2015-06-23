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

  describe('Controller: CentreStudiesPanelCtrl', function() {
    var createEntities, createController, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(fakeDomainEntities) {
      fakeEntities = fakeDomainEntities;
      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();
    }));

    function setupEntities(injector) {
      var Centre = injector.get('Centre'),
          Study = injector.get('Study');

      return create;

      //---

      function create() {
        var entities = {};
        entities.centre = new Centre(fakeEntities.centre());
        entities.studies = _.map(_.range(3), function () {
          return new Study(fakeEntities.study());
        });
        return entities;
      }

    }

    function setupController(injector) {
      var $rootScope   = injector.get('$rootScope'),
          $controller  = injector.get('$controller'),
          Panel        = injector.get('Panel'),
          Study        = injector.get('Study'),
          StudyViewer  = injector.get('StudyViewer'),
          tableService = injector.get('tableService'),
          modalService = injector.get('modalService');

      return create;

      //--

      function create(entities) {
        var scope = $rootScope.$new();

        scope.centre = entities.centre;
        scope.studyNames = studyNames(entities.studies);

        $controller('CentreStudiesPanelCtrl as vm', {
          $scope:       scope,
          Panel:        Panel,
          Study:        Study,
          StudyViewer:  StudyViewer,
          tableService: tableService,
          modalService: modalService
        });
        scope.$digest();
        return scope;
      }
    }

    function studyNameDto(study) {
      return { id: study.id, name: study.name, status: study.status };
    }

    function studyNames(studies) {
      return _.map(studies, function (study) {
        return studyNameDto(study);
      });
    }

    it('has valid state for centre with no studies', function() {
      var entities = createEntities(),
          scope = createController(entities);

      expect(scope.vm.centre).toBe(entities.centre);
      expect(scope.vm.studyNames.length).toBe(entities.studies.length);
      expect(scope.vm.studyNames).toContainAll(studyNames(entities.studies));
      expect(scope.vm.tableStudies).toBeDefined();
      expect(scope.vm.tableParams).toBeDefined();

      _.each(entities.studies, function (study) {
        expect(scope.vm.studyNamesById[study.id].id).toBe(study.id);
        expect(scope.vm.studyNamesById[study.id].name).toBe(study.name);
        expect(scope.vm.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    it('has valid state for centre with studies', function() {
      var entities = createEntities(),
          linkedStudy = entities.studies[0],
          scope;

      entities.centre.studyIds.push(linkedStudy.id);
      scope = createController(entities);

      expect(scope.vm.centre).toBe(entities.centre);
      expect(scope.vm.studyNames.length).toBe(entities.studies.length);
      expect(scope.vm.studyNames).toContainAll(studyNames(entities.studies));
      expect(scope.vm.tableParams).toBeDefined();

      _.each(entities.studies, function (study) {
        expect(scope.vm.tableStudies).toContain(studyNameDto(linkedStudy));

        expect(scope.vm.studyNamesById[study.id].id).toBe(study.id);
        expect(scope.vm.studyNamesById[study.id].name).toBe(study.name);
        expect(scope.vm.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    it('adds a study when selected', function() {
      var $q           = this.$injector.get('$q'),
          entities     = createEntities(),
          scope        = createController(entities),
          studyToAdd   = entities.studies[1];

      spyOn(entities.centre, 'addStudy').and.callFake(function () {
        return $q.when(entities.centre);
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});

      scope.vm.onSelect(studyToAdd);
      scope.$digest();
      expect(scope.vm.tableParams.reload).toHaveBeenCalled();
      expect(scope.vm.tableStudies).toContain(studyNameDto(studyToAdd));
    });

    it('study viewer is displayed', function() {
      var $q           = this.$injector.get('$q'),
          EntityViewer = this.$injector.get('EntityViewer'),
          Study        = this.$injector.get('Study'),
          entities     = createEntities(),
          scope        = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () {});
      spyOn(Study, 'get').and.callFake(function () {
        return $q.when(entities.studies[0]);
      });
      scope.vm.information(entities.studies[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('study is removed', function() {
      var $q            = this.$injector.get('$q'),
          modalService  = this.$injector.get('modalService'),
          entities      = createEntities(),
          studyToRemove = entities.studies[1],
          scope;

      entities.centre.studyIds.push(studyToRemove.id);
      scope = createController(entities);

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn(entities.centre, 'removeStudy').and.callFake(function () {
        return $q.when(entities.centre);
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});

      scope.vm.remove(studyToRemove.id);
      scope.$digest();
      expect(scope.vm.tableParams.reload).toHaveBeenCalled();
      expect(scope.vm.tableStudies).not.toContain(studyNameDto(studyToRemove));
    });

    it('displays remove failed information modal if remove fails', function() {
      var $q            = this.$injector.get('$q'),
          modalService  = this.$injector.get('modalService'),
          entities      = createEntities(),
          scope         = createController(entities),
          studyToRemove = entities.studies[1];

      spyOn(entities.centre, 'removeStudy').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });

      scope.vm.remove(studyToRemove.id);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});
