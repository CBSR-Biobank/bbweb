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

  describe('Directive: centreStudiesPanelDirective', function() {
    var scope, createEntities, createController, jsonEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite, testUtils) {
      _.extend(this, directiveTestSuite);

      jsonEntities = this.$injector.get('jsonEntities');
      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();

      this.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreStudiesPanel/centreStudiesPanel.html');
    }));

    function setupEntities(injector) {
      var Centre = injector.get('Centre'),
          Study = injector.get('Study');

      return create;

      //---

      function create() {
        var entities = {};
        entities.centre = new Centre(jsonEntities.centre());
        entities.studies = _.map(_.range(3), function () {
          return new Study(jsonEntities.study());
        });
        return entities;
      }

    }

    function setupController(injector) {
      var $rootScope = injector.get('$rootScope'),
          $compile   = injector.get('$compile');

      return create;

      //--

      function create(entities) {
        var element = angular.element([
          '<uib-accordion close-others="false">',
          '  <centre-studies-panel',
          '    centre="vm.centre" ',
          '    centre-studies="vm.centreStudies" ',
          '    study-names="vm.studyNames"> ',
          '  </centre-studies-panel>',
          '</uib-accordion>'
        ].join(''));

        // must have at least 2 studies in entities.studies
        expect(entities.studies.length).toBeGreaterThan(1);

        scope = $rootScope.$new();
        scope.vm = {
          centre:        entities.centre,
          centreStudies: _.map(entities.studies.slice(0, 2), function (study) { return study.id; }),
          studyNames:    studyNames(entities.studies)
        };

        $compile(element)(scope);
        scope.$digest();
        return element.find('centre-studies-panel').controller('centreStudiesPanel');
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
          controller = createController(entities);

      expect(controller.centre).toBe(entities.centre);
      expect(controller.studyNames.length).toBe(entities.studies.length);
      expect(controller.studyNames).toContainAll(studyNames(entities.studies));
      expect(controller.tableStudies).toBeDefined();

      _.each(entities.studies, function (study) {
        expect(controller.studyNamesById[study.id].id).toBe(study.id);
        expect(controller.studyNamesById[study.id].name).toBe(study.name);
        expect(controller.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    it('has valid state for centre with studies', function() {
      var entities = createEntities(),
          linkedStudy = entities.studies[0],
          controller;

      entities.centre.studyIds.push(linkedStudy.id);
      controller = createController(entities);

      expect(controller.centre).toBe(entities.centre);
      expect(controller.studyNames.length).toBe(entities.studies.length);
      expect(controller.studyNames).toContainAll(studyNames(entities.studies));

      _.each(entities.studies, function (study) {
        expect(controller.tableStudies).toContain(studyNameDto(linkedStudy));

        expect(controller.studyNamesById[study.id].id).toBe(study.id);
        expect(controller.studyNamesById[study.id].name).toBe(study.name);
        expect(controller.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    function studyOnSelectCommon(injector, entities) {
      var $q = injector.get('$q');
      spyOn(entities.centre, 'addStudy').and.callFake(function () {
        return $q.when(entities.centre);
      });
    }

    it('adds a new study when selected', function() {
      var entities   = createEntities(),
          controller = createController(entities),
          studyToAdd = entities.studies[2],
          numStudiesBeforeAdd;

      // studiesToAdd[2] is NOT one of the studies already associated with the centre

      studyOnSelectCommon(this.$injector, entities);
      numStudiesBeforeAdd = controller.tableStudies.length;
      controller.onSelect(studyToAdd);
      scope.$digest();
      expect(controller.tableStudies).toContain(studyNameDto(studyToAdd));
      expect(controller.tableStudies.length).toBe(numStudiesBeforeAdd + 1);
    });

    it('does not add an exiting study when selected', function() {
      var entities   = createEntities(),
          controller = createController(entities),
          studyToAdd = entities.studies[1],
          numStudiesBeforeAdd;

      // studiesToAdd[1] is already associated with the centre

      studyOnSelectCommon(this.$injector, entities);
      numStudiesBeforeAdd = controller.tableStudies.length;
      controller.onSelect(studyToAdd);
      scope.$digest();
      expect(controller.tableStudies).toContain(studyNameDto(studyToAdd));
      expect(controller.tableStudies.length).toBe(numStudiesBeforeAdd);
    });

    it('study viewer is displayed', function() {
      var $q           = this.$injector.get('$q'),
          EntityViewer = this.$injector.get('EntityViewer'),
          Study        = this.$injector.get('Study'),
          entities     = createEntities(),
          controller   = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () {});
      spyOn(Study, 'get').and.callFake(function () {
        return $q.when(entities.studies[0]);
      });
      controller.information(entities.studies[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('study is removed', function() {
      var $q            = this.$injector.get('$q'),
          modalService  = this.$injector.get('modalService'),
          entities      = createEntities(),
          studyToRemove = entities.studies[1],
          controller;

      entities.centre.studyIds.push(studyToRemove.id);
      controller = createController(entities);

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn(entities.centre, 'removeStudy').and.callFake(function () {
        return $q.when(entities.centre);
      });

      controller.remove(studyToRemove.id);
      scope.$digest();
      expect(controller.tableStudies).not.toContain(studyNameDto(studyToRemove));
    });

    it('displays remove failed information modal if remove fails', function() {
      var $q            = this.$injector.get('$q'),
          modalService  = this.$injector.get('modalService'),
          entities      = createEntities(),
          controller    = createController(entities),
          studyToRemove = entities.studies[1];

      spyOn(entities.centre, 'removeStudy').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });

      controller.remove(studyToRemove.id);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});
