/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: centreStudiesPanelDirective', function() {

    var studyNameDto = function (study) {
      return {
        id:          study.id,
        name:        study.name,
        status:      study.status,
        statusLabel: this.studyStatusLabel.statusToLabel(study.status)
      };
    };

    var studyNames = function (studies) {
      var self = this;
      return _.map(studies, function (study) {
        return studyNameDto.call(self, study);
      });
    };

    var createEntities = function () {
       var self = this,
           entities = {};

      entities.centre = new self.Centre(self.factory.centre());
      entities.studies = _.map(_.range(3), function () {
        return new self.Study(self.factory.study());
      });
      return entities;
    };

    var createController = function (entities) {
      var element = angular.element([
        '<centre-studies-panel',
        '  centre="vm.centre" ',
        '  centre-studies="vm.centreStudies" ',
        '  study-names="vm.studyNames"> ',
        '</centre-studies-panel>'
      ].join(''));

      // must have at least 2 studies in entities.studies
      expect(entities.studies.length).toBeGreaterThan(1);

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        centre:        entities.centre,
        centreStudies: _.map(entities.studies.slice(0, 2), function (study) { return study.id; }),
        studyNames:    studyNames.call(this, entities.studies)
      };

      this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
      this.scope.$on('centre-view', this.eventRxFunc);

      this.$compile(element)(this.scope);
      this.scope.$digest();
      this.controller = element.controller('centreStudiesPanel');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Centre',
                              'Study',
                              'EntityViewer',
                              'modalService',
                              'studyStatusLabel',
                              'factory');

      testUtils.addCustomMatchers();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreStudiesPanel/centreStudiesPanel.html');
    }));

    it('event is emitted to parent on initialization', function() {
      var entities = createEntities.call(this);
      createController.call(this, entities);
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

    it('has valid state for centre with no studies', function() {
      var self = this,
          entities = createEntities.call(this);

      createController.call(this, entities);

      expect(self.controller.centre).toBe(entities.centre);
      expect(self.controller.studyNames.length).toBe(entities.studies.length);
      expect(self.controller.studyNames).toContainAll(studyNames.call(this, entities.studies));
      expect(self.controller.studyCollection).toBeDefined();

      _.each(entities.studies, function (study) {
        expect(self.controller.studyNamesById[study.id].id).toBe(study.id);
        expect(self.controller.studyNamesById[study.id].name).toBe(study.name);
        expect(self.controller.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    it('has valid state for centre with studies', function() {
      var self = this,
          entities = createEntities.call(this),
          linkedStudy = entities.studies[0];

      entities.centre.studyIds.push(linkedStudy.id);
      createController.call(this, entities);

      expect(self.controller.centre).toBe(entities.centre);
      expect(self.controller.studyNames.length).toBe(entities.studies.length);
      expect(self.controller.studyNames).toContainAll(studyNames.call(this, entities.studies));

      _.each(entities.studies, function (study) {
        expect(self.controller.studyCollection).toContain(studyNameDto.call(self, linkedStudy));

        expect(self.controller.studyNamesById[study.id].id).toBe(study.id);
        expect(self.controller.studyNamesById[study.id].name).toBe(study.name);
        expect(self.controller.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    it('adds a new study when selected', function() {
      var entities   = createEntities.call(this),
          studyToAdd = entities.studies[2],
          numStudiesBeforeAdd;

      // studiesToAdd[2] is NOT one of the studies already associated with the centre

      createController.call(this, entities);
      spyOn(entities.centre, 'addStudy').and.returnValue(this.$q.when(entities.centre));
      numStudiesBeforeAdd = this.controller.studyCollection.length;
      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.controller.studyCollection).toContain(studyNameDto.call(this, studyToAdd));
      expect(this.controller.studyCollection.length).toBe(numStudiesBeforeAdd + 1);
    });

    it('does not add an exiting study when selected', function() {
      var entities   = createEntities.call(this),
          studyToAdd = entities.studies[1],
          numStudiesBeforeAdd;

      // studiesToAdd[1] is already associated with the centre

      createController.call(this, entities);
      spyOn(entities.centre, 'addStudy').and.returnValue(this.$q.when(entities.centre));
      numStudiesBeforeAdd = this.controller.studyCollection.length;
      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.controller.studyCollection).toContain(studyNameDto.call(this, studyToAdd));
      expect(this.controller.studyCollection.length).toBe(numStudiesBeforeAdd + 1);
    });

    it('study viewer is displayed', function() {
      var entities     = createEntities.call(this);

      createController.call(this, entities);
      spyOn(this.EntityViewer.prototype, 'showModal').and.callFake(function () {});
      spyOn(this.Study, 'get').and.returnValue(this.$q.when(entities.studies[0]));
      this.controller.information(entities.studies[0].id);
      this.scope.$digest();
      expect(this.EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('study is removed', function() {
      var entities      = createEntities.call(this),
          studyToRemove = entities.studies[1];

      entities.centre.studyIds.push(studyToRemove.id);

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(entities.centre, 'removeStudy').and.returnValue(this.$q.when(entities.centre));

      createController.call(this, entities);
      this.controller.remove(studyToRemove.id);
      this.scope.$digest();
      expect(this.controller.studyCollection).not.toContain(studyNameDto.call(this, studyToRemove));
    });

    it('displays remove failed information modal if remove fails', function() {
      var deferred      = this.$q.defer(),
          entities      = createEntities.call(this),
          studyToRemove = entities.studies[1];

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(entities.centre, 'removeStudy').and.returnValue(deferred.promise);

      deferred.reject('err');
      createController.call(this, entities);
      this.controller.remove(studyToRemove.id);
      this.scope.$digest();
      expect(this.modalService.showModal.calls.count()).toBe(2);
    });

  });

});
