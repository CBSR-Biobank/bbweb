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

  describe('Component: centreStudiesPanel', function() {

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

      SuiteMixin.prototype.createController = function (entities) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<centre-studies-panel',
            '  centre="vm.centre" ',
            '  centre-studies="vm.centreStudies" ',
            '  study-names="vm.studyNames"> ',
            '</centre-studies-panel>'
          ].join(''),
          {
            centre:        entities.centre,
            centreStudies: _.map(entities.studies.slice(0, 2), function (study) { return study.id; }),
            studyNames:    this.studyNames(entities.studies)
          },
          'centreStudiesPanel');
      };

      SuiteMixin.prototype.studyNameDto = function (study) {
        return {
          id:    study.id,
          name:  study.name,
          state: study.state
        };
      };

      SuiteMixin.prototype.studyNames = function (studies) {
        var self = this;
        return _.map(studies, function (study) {
          return self.studyNameDto(study);
        });
      };

      SuiteMixin.prototype.createEntities = function () {
        var self = this,
            studies = _.map(_.range(3), function () {
              return new self.Study(self.factory.study());
            }),
            centre = new this.Centre(self.factory.centre());
        return { centre: centre, studies: studies};
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin, testUtils) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Centre',
                              'CentreState',
                              'Study',
                              'EntityViewer',
                              'modalService',
                              'factory');

      testUtils.addCustomMatchers();

      this.putHtmlTemplates(
        '/assets/javascripts/admin/centres/components/centreStudiesPanel/centreStudiesPanel.html');
    }));

    it('event is emitted to parent on initialization', function() {
      var entities = this.createEntities();
      this.createController(entities);
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

    it('has valid state for centre with no studies', function() {
      var self = this,
          entities = this.createEntities();

      this.createController(entities);

      expect(self.controller.centre).toBe(entities.centre);
      expect(self.controller.studyNames.length).toBe(entities.studies.length);
      expect(self.controller.studyNames).toContainAll(this.studyNames(entities.studies));
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
      expect(self.controller.studyNames).toContainAll(this.studyNames(entities.studies));

      _.each(entities.studies, function (study) {
        expect(self.controller.studyCollection).toContain(self.studyNameDto(linkedStudy));

        expect(self.controller.studyNamesById[study.id].id).toBe(study.id);
        expect(self.controller.studyNamesById[study.id].name).toBe(study.name);
        expect(self.controller.studyNamesById[study.id].status).toBe(study.status);
      });
    });

    describe('when a study is selected', function() {

      beforeEach(function() {
        this.entities = this.createEntities();
        spyOn(this.Centre.prototype, 'addStudy').and.returnValue(this.$q.when(this.entities.centre));
        this.entities.centre.studyIds = [ this.entities.studies[1].id ];
        this.createController(this.entities);
      });

      it('adds a new study when selected', function() {
        var numStudiesBeforeAdd = this.controller.studyCollection.length,
            studyToAdd = this.entities.studies[2];

        // studiesToAdd[2] is NOT one of the studies already associated with the centre

        this.controller.onSelect(studyToAdd);
        this.scope.$digest();
        expect(this.controller.studyCollection).toContain(this.studyNameDto(studyToAdd));
        expect(this.controller.studyCollection.length).toBe(numStudiesBeforeAdd + 1);
      });

      it('does not add an exiting study when selected', function() {
        var numStudiesBeforeAdd = this.controller.studyCollection.length,
            studyToAdd = this.entities.studies[1];

        // studiesToAdd[1] is already associated with the centre
        expect(this.controller.studyCollection).toContain(this.studyNameDto(studyToAdd));

        this.controller.onSelect(studyToAdd);
        this.scope.$digest();

        expect(this.controller.studyCollection).toContain(this.studyNameDto(studyToAdd));
        expect(this.controller.studyCollection.length).toBe(numStudiesBeforeAdd);
      });

      it('if centre is not disabled, an exception is thrown', function() {
        var self = this,
            studyToAdd = this.entities.studies[0];

        this.entities.centre.state = this.CentreState.ENABLED;
        expect(function () {
          self.controller.onSelect(studyToAdd);
        }).toThrowError(/An application error occurred/);
      });

    });

    it('study viewer is displayed', function() {
      var entities = this.createEntities();

      this.createController(entities);
      spyOn(this.EntityViewer.prototype, 'showModal').and.returnValue(this.$q.when('ok'));
      spyOn(this.Study, 'get').and.returnValue(this.$q.when(entities.studies[0]));
      this.controller.information(entities.studies[0].id);
      this.scope.$digest();
      expect(this.EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('study is removed', function() {
      var entities      = this.createEntities(),
          studyToRemove = entities.studies[1];

      entities.centre.studyIds.push(studyToRemove.id);

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
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

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(entities.centre, 'removeStudy').and.returnValue(deferred.promise);

      deferred.reject('err');
      this.createController(entities);
      this.controller.remove(studyToRemove.id);
      this.scope.$digest();
      expect(this.modalService.modalOkCancel.calls.count()).toBe(2);
    });

  });

});
