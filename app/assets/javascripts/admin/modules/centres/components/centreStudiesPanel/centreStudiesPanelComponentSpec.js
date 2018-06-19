/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('Component: centreStudiesPanel', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Centre',
                              'CentreState',
                              'Study',
                              'EntityViewer',
                              'modalService',
                              'Factory');

      this.addCustomMatchers();

      this.createScope = (scopeVars) => {
        var scope = ComponentTestSuiteMixin.createScope.call(this, scopeVars);
        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        scope.$on('tabbed-page-update', this.eventRxFunc);
        return scope;
      };

      this.createController = (entities) => {
        this.createControllerInternal(
          `<centre-studies-panel
             centre="vm.centre"
             centre-studies="vm.centreStudies"
             study-names="vm.studyNames">
           </centre-studies-panel>`,
          {
            centre:        entities.centre,
            centreStudies: entities.studies.slice(0, 2).map((study) => study.id),
            studyNames:    this.studyNames(entities.studies)
          },
          'centreStudiesPanel');
      };

      this.studyNameDto = (study) => ({
        id:    study.id,
        name:  study.name,
        state: study.state
      });

      this.studyNames = (studies) => studies.map((study) => this.studyNameDto(study));

      this.createEntities = () => {
        var studies = _.range(3).map(() =>  this.Study.create(this.Factory.study())),
            centre = this.Centre.create(this.Factory.centre());
        return { centre: centre, studies: studies};
      };
    });
  });

  it('event is emitted to parent on initialization', function() {
    var entities = this.createEntities();
    this.createController(entities);
    expect(this.eventRxFunc).toHaveBeenCalled();
  });

  it('has valid state for centre with studies', function() {
    var self = this,
        entities = this.createEntities(),
        linkedStudy = entities.studies[0],
        linkedStudyName = this.studyNameDto(linkedStudy);

    entities.centre.studyNames.push(linkedStudyName);
    this.createController(entities);

    expect(self.controller.centre).toBe(entities.centre);
    expect(self.controller.centre.studyNames.length).toBe(1);
    expect(self.controller.centre.studyNames).toContain(linkedStudyName);
  });

  describe('when a study is selected', function() {

    beforeEach(function() {
      this.entities = this.createEntities();
      this.Centre.prototype.addStudy = jasmine.createSpy().and.returnValue(this.$q.when(this.entities.centre));
      this.entities.centre.studyNames = [ this.studyNameDto(this.entities.studies[1]) ];
      this.createController(this.entities);
    });

    it('adds a new study when selected', function() {
      var studyToAdd = this.studyNameDto(this.entities.studies[2]);

      // studiesToAdd[2] is NOT one of the studies already associated with the centre

      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.Centre.prototype.addStudy).toHaveBeenCalled();
    });

    it('does not add an exiting study when selected', function() {
      var studyToAdd = this.studyNameDto(this.entities.studies[1]);

      // studiesToAdd[1] is already associated with the centre
      expect(this.controller.centre.studyNames).toContain(studyToAdd);

      this.controller.onSelect(studyToAdd);
      this.scope.$digest();
      expect(this.Centre.prototype.addStudy).not.toHaveBeenCalled();
    });

    it('if centre is not disabled, an exception is thrown', function() {
      var self = this,
          studyToAdd = this.studyNameDto(this.entities.studies[1]);

      this.entities.centre.state = this.CentreState.ENABLED;
      expect(function () {
        self.controller.onSelect(studyToAdd);
      }).toThrowError(/An application error occurred/);
    });

  });

  it('study is removed', function() {
    var entities      = this.createEntities(),
        studyToRemove = this.studyNameDto(entities.studies[1]);

    entities.centre.studyNames.push(studyToRemove);

    spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
    spyOn(entities.centre, 'removeStudy').and.returnValue(this.$q.when(entities.centre));

    this.createController(entities);
    this.controller.remove(studyToRemove);
    this.scope.$digest();
    expect(entities.centre.removeStudy).toHaveBeenCalled();
  });

  it('displays remove failed information modal if remove fails', function() {
    var entities      = this.createEntities(),
        studyToRemove = this.studyNameDto(entities.studies[1]);

    spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));

    this.createController(entities);
    spyOn(entities.centre, 'removeStudy').and.returnValue(this.$q.reject('simulated error'));
    this.controller.remove(studyToRemove.id);
    this.scope.$digest();
    expect(this.modalService.modalOkCancel.calls.count()).toBe(2);
  });

});
