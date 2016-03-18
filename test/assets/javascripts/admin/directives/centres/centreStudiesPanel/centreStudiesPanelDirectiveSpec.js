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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q           = self.$injector.get('$q');
      self.Centre       = self.$injector.get('Centre');
      self.Study        = self.$injector.get('Study');
      self.EntityViewer = this.$injector.get('EntityViewer');
      self.modalService = this.$injector.get('modalService');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.createEntities = createEntities;
      self.createController = createController;
      this.studyOnSelectCommon = studyOnSelectCommon;

      testUtils.addCustomMatchers();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreStudiesPanel/centreStudiesPanel.html');

      ///----

      function createEntities() {
        var entities = {};
        entities.centre = new self.Centre(self.jsonEntities.centre());
        entities.studies = _.map(_.range(3), function () {
          return new self.Study(self.jsonEntities.study());
        });
        return entities;
      }

      function createController(entities) {
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

        self.scope = $rootScope.$new();
        self.scope.vm = {
          centre:        entities.centre,
          centreStudies: _.map(entities.studies.slice(0, 2), function (study) { return study.id; }),
          studyNames:    studyNames(entities.studies)
        };

        $compile(element)(self.scope);
        self.scope.$digest();
        self.controller = element.find('centre-studies-panel').controller('centreStudiesPanel');
      }

      function studyOnSelectCommon(entities) {
        spyOn(entities.centre, 'addStudy').and.returnValue(self.$q.when(entities.centre));
      }
    }));

    it('has valid state for centre with no studies', function() {
      var self = this,
          entities = this.createEntities();

      self.createController(entities);

      expect(self.controller.centre).toBe(entities.centre);
      expect(self.controller.studyNames.length).toBe(entities.studies.length);
      expect(self.controller.studyNames).toContainAll(studyNames(entities.studies));
      expect(self.controller.tableStudies).toBeDefined();

      _.each(entities.studies, function (study) {
        expect(self.controller.studyNamesById[study.id].id).toBe(study.id);
        expect(self.controller.studyNamesById[study.id].name).toBe(study.name);
        expect(self.controller.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    it('has valid state for centre with studies', function() {
      var self = this,
          entities = this.createEntities(),
          linkedStudy = entities.studies[0];

      entities.centre.studyIds.push(linkedStudy.id);
      this.createController(entities);

      expect(self.controller.centre).toBe(entities.centre);
      expect(self.controller.studyNames.length).toBe(entities.studies.length);
      expect(self.controller.studyNames).toContainAll(studyNames(entities.studies));

      _.each(entities.studies, function (study) {
        expect(self.controller.tableStudies).toContain(studyNameDto(linkedStudy));

        expect(self.controller.studyNamesById[study.id].id).toBe(study.id);
        expect(self.controller.studyNamesById[study.id].name).toBe(study.name);
        expect(self.controller.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    it('adds a new study when selected', function() {
      var entities   = this.createEntities(),
          studyToAdd = entities.studies[2],
          numStudiesBeforeAdd;

      // studiesToAdd[2] is NOT one of the studies already associated with the centre

      this.createController(entities);
      this.studyOnSelectCommon(entities);
      numStudiesBeforeAdd = this.controller.tableStudies.length;
      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.controller.tableStudies).toContain(studyNameDto(studyToAdd));
      expect(this.controller.tableStudies.length).toBe(numStudiesBeforeAdd + 1);
    });

    it('does not add an exiting study when selected', function() {
      var entities   = this.createEntities(),
          studyToAdd = entities.studies[1],
          numStudiesBeforeAdd;

      // studiesToAdd[1] is already associated with the centre

      this.createController(entities);
      this.studyOnSelectCommon(entities);
      numStudiesBeforeAdd = this.controller.tableStudies.length;
      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.controller.tableStudies).toContain(studyNameDto(studyToAdd));
      expect(this.controller.tableStudies.length).toBe(numStudiesBeforeAdd);
    });

    it('study viewer is displayed', function() {
      var entities     = this.createEntities();

      this.createController(entities);
      spyOn(this.EntityViewer.prototype, 'showModal').and.callFake(function () {});
      spyOn(this.Study, 'get').and.returnValue(this.$q.when(entities.studies[0]));
      this.controller.information(entities.studies[0].id);
      this.scope.$digest();
      expect(this.EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('study is removed', function() {
      var entities      = this.createEntities(),
          studyToRemove = entities.studies[1];

      entities.centre.studyIds.push(studyToRemove.id);

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(entities.centre, 'removeStudy').and.returnValue(this.$q.when(entities.centre));

      this.createController(entities);
      this.controller.remove(studyToRemove.id);
      this.scope.$digest();
      expect(this.controller.tableStudies).not.toContain(studyNameDto(studyToRemove));
    });

    it('displays remove failed information modal if remove fails', function() {
      var deferred      = this.$q.defer(),
          entities      = this.createEntities(),
          studyToRemove = entities.studies[1];

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(entities.centre, 'removeStudy').and.returnValue(deferred.promise);

      deferred.reject('err');
      this.createController(entities);
      this.controller.remove(studyToRemove.id);
      this.scope.$digest();
      expect(this.modalService.showModal.calls.count()).toBe(2);
    });

    function studyNameDto(study) {
      return { id: study.id, name: study.name, status: study.status };
    }

    function studyNames(studies) {
      return _.map(studies, function (study) {
        return studyNameDto(study);
      });
    }

  });

});
