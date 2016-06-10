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

    beforeEach(inject(function($rootScope, $compile, templateMixin, testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q               = self.$injector.get('$q');
      self.Centre           = self.$injector.get('Centre');
      self.Study            = self.$injector.get('Study');
      self.EntityViewer     = this.$injector.get('EntityViewer');
      self.modalService     = this.$injector.get('modalService');
      self.studyStatusLabel = self.$injector.get('studyStatusLabel');
      self.factory     = self.$injector.get('factory');

      self.createEntities = createEntities;
      self.createController = createController;
      self.studyOnSelectCommon = studyOnSelectCommon;
      self.studyNameDto = studyNameDto;
      self.studyNames = studyNames;

      testUtils.addCustomMatchers();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreStudiesPanel/centreStudiesPanel.html');

      ///----

      function createEntities() {
        var entities = {};
        entities.centre = new self.Centre(self.factory.centre());
        entities.studies = _.map(_.range(3), function () {
          return new self.Study(self.factory.study());
        });
        return entities;
      }

      function createController(entities) {
        var element = angular.element([
          '<centre-studies-panel',
          '  centre="vm.centre" ',
          '  centre-studies="vm.centreStudies" ',
          '  study-names="vm.studyNames"> ',
          '</centre-studies-panel>'
        ].join(''));

        // must have at least 2 studies in entities.studies
        expect(entities.studies.length).toBeGreaterThan(1);

        self.scope = $rootScope.$new();
        self.scope.vm = {
          centre:        entities.centre,
          centreStudies: _.map(entities.studies.slice(0, 2), function (study) { return study.id; }),
          studyNames:    self.studyNames(entities.studies)
        };

        $compile(element)(self.scope);
        self.scope.$digest();
        self.controller = element.controller('centreStudiesPanel');
      }

      function studyOnSelectCommon(entities) {
        spyOn(entities.centre, 'addStudy').and.returnValue(self.$q.when(entities.centre));
      }

      function studyNameDto(study) {
        return {
          id:          study.id,
          name:        study.name,
          status:      study.status,
          statusLabel: self.studyStatusLabel.statusToLabel(study.status)
        };
      }

      function studyNames(studies) {
        return _.map(studies, function (study) {
          return self.studyNameDto(study);
        });
      }
    }));

    it('has valid state for centre with no studies', function() {
      var self = this,
          entities = this.createEntities();

      self.createController(entities);

      expect(self.controller.centre).toBe(entities.centre);
      expect(self.controller.studyNames.length).toBe(entities.studies.length);
      expect(self.controller.studyNames).toContainAll(self.studyNames(entities.studies));
      expect(self.controller.studyCollection).toBeDefined();

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
      expect(self.controller.studyNames).toContainAll(self.studyNames(entities.studies));

      _.each(entities.studies, function (study) {
        expect(self.controller.studyCollection).toContain(self.studyNameDto(linkedStudy));

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
      numStudiesBeforeAdd = this.controller.studyCollection.length;
      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.controller.studyCollection).toContain(this.studyNameDto(studyToAdd));
      expect(this.controller.studyCollection.length).toBe(numStudiesBeforeAdd + 1);
    });

    it('does not add an exiting study when selected', function() {
      var entities   = this.createEntities(),
          studyToAdd = entities.studies[1],
          numStudiesBeforeAdd;

      // studiesToAdd[1] is already associated with the centre

      this.createController(entities);
      this.studyOnSelectCommon(entities);
      numStudiesBeforeAdd = this.controller.studyCollection.length;
      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.controller.studyCollection).toContain(this.studyNameDto(studyToAdd));
      expect(this.controller.studyCollection.length).toBe(numStudiesBeforeAdd + 1);
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
      expect(this.controller.studyCollection).not.toContain(this.studyNameDto(studyToRemove));
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

  });

});
