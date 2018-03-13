/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: selectStudy', function() {

  const panelHeader = 'selectStudy component header';

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Study', 'Factory');

      this.createController = (scopeVars) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          `<select-study
              header="${panelHeader}"
              get-studies="vm.getStudies"
              limit="vm.limit"
              message-no-results="No results match the criteria."
              icon="glyphicon-ok-circle"
              on-study-selected="vm.onStudySelected">
           </select-study>'`,
          scopeVars,
          'selectStudy');
      };

      this.createStudies = (numStudies) =>
        _.range(numStudies).map(() => this.Study.create(this.Factory.study()));

      this.createGetStudiesFn = (studies) =>
        (pagerOptions) => this.$q.when(_.extend(this.Factory.pagedResult(studies, pagerOptions),
                                               { items: studies.slice(0, pagerOptions.limit) }));
    });
  });

  it('displays the list of studies', function() {
    var studies = this.createStudies(20),
        limit = studies.length / 2;

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: limit
    });

    expect(this.element.find('li.list-group-item').length).toBe(limit);
    expect(this.element.find('input').length).toBe(1);
  });

  it('displays the pannel header correctly', function() {
    var studies = this.createStudies(20),
        limit = studies.length / 2;

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: limit
    });
    expect(this.element.find('h3').text()).toBe(panelHeader);
  });

  it('has a name filter', function() {
    var studies = this.createStudies(20);

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: studies.length
    });
    expect(this.element.find('input').length).toBe(1);
  });

  it('displays pagination controls', function() {
    var studies = this.createStudies(20),
        limit = studies.length / 2;

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: limit
    });

    expect(this.controller.showPagination).toBe(true);
    expect(this.element.find('ul.pagination-sm').length).toBe(1);
  });

  it('updates to name filter cause studies to be re-loaded', function() {
    var studies = this.createStudies(20),
        limit = studies.length / 2;

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: limit
    });

    spyOn(this.controller, 'getStudies').and.callThrough();

    [
      { callCount: 1, nameFilter: 'test' },
      { callCount: 2, nameFilter: '' }
    ].forEach((obj) => {
      this.controller.nameFilter = obj.nameFilter;
      this.controller.nameFilterUpdated();
      expect(this.controller.getStudies.calls.count()).toBe(obj.callCount);
    });
  });

  it('page change causes studies to be re-loaded', function() {
    var studies = this.createStudies(20),
        limit = studies.length / 2;

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: limit
    });

    spyOn(this.controller, 'getStudies').and.callThrough();
    this.controller.pageChanged();
    expect(this.controller.getStudies).toHaveBeenCalled();
  });

  it('clear filter causes studies to be re-loaded', function() {
    var studies = this.createStudies(20),
        limit = studies.length / 2;

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: limit
    });

    spyOn(this.controller, 'getStudies').and.callThrough();
    this.controller.clearFilter();
    expect(this.controller.getStudies).toHaveBeenCalled();
  });

  it('studyGlyphicon returns valid image tag', function() {
    var studies = this.createStudies(20),
        limit = studies.length / 2,
        studyToNavigateTo = studies[0];

    this.createController({
      getStudies: this.createGetStudiesFn(studies),
      limit: limit
    });

    expect(this.controller.studyGlyphicon(studyToNavigateTo))
      .toEqual('<i class="glyphicon glyphicon-ok-circle"></i>');
  });

  describe('when selecting a study', function() {

    beforeEach(function() {
      this.injectDependencies('$state', 'Study');
      this.study = new this.Study(this.Factory.study());
    });

    it('a state change is triggered when a study is selected', function() {
      var location = this.Factory.location(),
          centre = this.Factory.centre({ locations: [ location ] }),
          centreLocations = this.Factory.centreLocations([ centre ]),
          onStudySelected = jasmine.createSpy().and.returnValue(null),
          args;

      spyOn(this.Study.prototype, 'allLocations').and.returnValue(this.$q.when(centreLocations));

      this.createController({
        getStudies:      this.createGetStudiesFn([]),
        limit:           1,
        onStudySelected: onStudySelected
      });

      this.controller.studySelected(this.study);
      this.scope.$digest();
      expect(onStudySelected).toHaveBeenCalled();

      args = onStudySelected.calls.argsFor(0);
      expect(args[0]).toBe(this.study);
    });

    it('when the selected study does not have centres associated with it', function() {
      this.injectDependencies('modalService');

      spyOn(this.Study.prototype, 'allLocations').and.returnValue(this.$q.when([]));
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));

      this.createController({
        getStudies: this.createGetStudiesFn([]),
        limit: 1
      });

      this.controller.studySelected(this.study);
      this.scope.$digest();
      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

  });

});
