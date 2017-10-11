/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: selectStudy', function() {

  const panelHeader = 'selectStudy component header',
        navigateStateName = 'test-navigate-state-name',
        navigateStateParamName = 'test-navigate-state-param-name',
        getHeader = () => panelHeader;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Study', 'factory');

      this.createController = (scopeVars) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          `<select-study get-header="vm.getHeader"
             get-studies="vm.getStudies"
             icon="glyphicon-ok-circle"
             limit="vm.limit"
             message-no-results="No results match the criteria."
             navigate-state-name="${navigateStateName}"
             navigate-state-param-name="${navigateStateParamName}">
           </select-study>'`,
          _.extend({ getHeader:  getHeader }, scopeVars),
          'selectStudy');
      };

      this.createStudies = (numStudies) =>
        _.range(numStudies).map(() => this.Study.create(this.factory.study()));

      this.createGetStudiesFn = (studies) =>
        (pagerOptions) => this.$q.when(_.extend(this.factory.pagedResult(studies, pagerOptions),
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
      this.study = new this.Study(this.factory.study());
    });

    it('a state change is triggered when a study is selected', function() {
      var location = this.factory.location(),
          centre = this.factory.centre({ locations: [ location ] }),
          centreLocations = this.factory.centreLocations([ centre ]),
          args;

      spyOn(this.$state, 'go').and.returnValue(null);
      spyOn(this.Study.prototype, 'allLocations').and.returnValue(this.$q.when(centreLocations));

      this.createController({
        getStudies: this.createGetStudiesFn([]),
        limit: 1
      });

      this.controller.studySelected(this.study);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalled();

      args = this.$state.go.calls.argsFor(0);
      expect(args[0]).toEqual(navigateStateName);
      expect(args[1][navigateStateParamName]).toEqual(this.study.id);
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
