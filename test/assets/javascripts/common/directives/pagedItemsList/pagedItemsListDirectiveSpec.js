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

  describe('Directive: pagedItemsListDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (factory) {
      this.factory = factory;
    }));

    describe('Centres', function () {
      var context = {};

      beforeEach(inject(function ($q, Centre, CentreStatus, centreStatusLabel) {
        var self = this,
            disabledCentres,
            enabledCentres;

        disabledCentres = _.map(_.range(2), function() {
          return new self.factory.centre();
        });
        enabledCentres = _.map(_.range(3), function() {
          var centre = new self.factory.centre();
          centre.status = CentreStatus.ENABLED;
          return centre;
        });

        context.entities                     = disabledCentres.concat(enabledCentres);
        context.counts                       = createCounts(disabledCentres.length,
                                                            enabledCentres.length);
        context.pageSize                     = disabledCentres.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.centres.centre.summary';
        context.entityNavigateStateParamName = 'centreId';

        context.possibleStatuses = [{ id: 'all', name: 'all' }].concat(
          _.map(_.values(CentreStatus), function (status) {
            return { id: status, name: centreStatusLabel.statusToLabel(status) };
          }));
      }));

      sharedBehaviour(context);
    });

    describe('Studies', function () {
      var context = {};

      beforeEach(inject(function ($q, Study, StudyStatus, studyStatusLabel) {
        var self = this,
            disabledStudies,
            enabledStudies,
            retiredStudies;

        disabledStudies = _.map(_.range(2), function() {
          return new self.factory.study();
        });
        enabledStudies = _.map(_.range(3), function() {
          var study = new self.factory.study();
          study.status = StudyStatus.ENABLED;
          return study;
        });
        retiredStudies = _.map(_.range(3), function() {
          var study = new self.factory.study();
          study.status = StudyStatus.RETIRED;
          return study;
        });

        context.entities                     = disabledStudies.concat(enabledStudies.concat(retiredStudies));
        context.counts                       = createCounts(disabledStudies.length,
                                                            enabledStudies.length,
                                                            retiredStudies.length);
        context.pageSize                     = disabledStudies.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.studies.study.summary';
        context.entityNavigateStateParamName = 'studyId';

        context.possibleStatuses = [{ id: 'all', name: 'all' }].concat(
          _.map(_.values(StudyStatus), function (status) {
            return { id: status, name: studyStatusLabel.statusToLabel(status) };
          }));
      }));

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function () {

        beforeEach(inject(function ($rootScope, $compile, templateMixin, testUtils) {
          var self = this;

          _.extend(self, templateMixin);

          self.$q                  = self.$injector.get('$q');
          self.context             = context;
          self.getItemsSpy         = jasmine.createSpy('getItemsSpy');
          self.createController    = createController;
          self.getItemsFuncDefault = getItemsFuncDefault;

          self.putHtmlTemplates(
            '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');

          testUtils.addCustomMatchers();

          //--

          function createController(getItemsFunc, getItemIconFunc) {
            var element = angular.element([
              '<paged-items-list',
              '  counts="vm.counts"',
              '  page-size="vm.pageSize"',
              '  possible-statuses="vm.possibleStatuses"',
              '  message-no-items="' + context.messageNoItems + '"',
              '  message-no-results="' + context.messageNoResults + '"',
              '  get-items="vm.getItems"',
              '  get-item-icon="vm.getItemIcon"',
              '  entity-navigate-state="' + context.entityNavigateState + '"',
              '  entity-navigate-state-param-name="' + context.entityNavigateStateParamName + '">',
              '</paged-items-list>'
            ].join(''));

            getItemsFunc = getItemsFunc || getItemsFuncDefault;
            getItemIconFunc = getItemIconFunc || function (entity) { return ''; };

            self.scope = $rootScope.$new();
            self.scope.vm = {
              counts:           context.counts,
              pageSize:         context.pageSize,
              possibleStatuses: context.possibleStatuses,
              getItems:         getItemsFunc,
              getItemIcon:      getItemIconFunc
            };

            $compile(element)(self.scope);
            self.scope.$digest();
            self.controller = element.controller('pagedItemsList');
          }

          /**
           * This is the default function that will be called by the directive to update it's contents.
           *
           * Passed to the directive as an external function.
           *
           * See createController().
           */
          function getItemsFuncDefault(options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    context.entities.slice(0, self.pageSize),
              page:     options.page,
              pageSize: self.pageSize,
              offset:   0,
              total:    context.entities.length
            });
          }
        }));

        it('has valid scope', function() {
          this.createController();

          expect(this.controller.counts).toBe(this.context.counts);
          expect(this.controller.possibleStatuses).toBe(this.context.possibleStatuses);
          expect(this.controller.sortFields).toContainAll(['Name', 'Status']);

          expect(this.controller.pagerOptions.filter).toBeEmptyString();
          expect(this.controller.pagerOptions.status).toBe(this.context.possibleStatuses[0].id);
          expect(this.controller.pagerOptions.pageSize).toBe(this.context.pageSize);
          expect(this.controller.pagerOptions.sort).toBe('name');
        });

        it('has a valid panel heading', function() {
          var self = this;

          self.createController(self.getItemsFuncDefault);

          _.each(context.possibleStatuses, function (status) {
            if (status.id !== 'all') {
              expect(self.controller.panelHeading).toContain(status.name);
            }
          });
        });

        it('updates items when name filter is updated', function() {
          var nameFilterValue = 'test';

          this.createController();

          this.controller.pagerOptions.filter = nameFilterValue;
          this.controller.nameFilterUpdated();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: nameFilterValue,
            status: this.context.possibleStatuses[0].id,
            page: 1,
            pageSize: this.context.pageSize,
            sort: 'name'
          });
        });

        it('updates items when name status filter is updated', function() {
          var statusFilterValue = this.context.possibleStatuses[1];

          this.createController();
          this.controller.pagerOptions.status = statusFilterValue;
          this.controller.statusFilterUpdated();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: statusFilterValue,
            page: 1,
            pageSize: this.context.pageSize,
            sort: this.controller.sortFields[0].toLowerCase()
          });
        });

        it('clears filters', function() {
          this.createController();
          this.controller.pagerOptions.filter = 'test';
          this.controller.pagerOptions.status = this.context.possibleStatuses[1];
          this.controller.clearFilters();
          this.scope.$digest();
          expect(this.controller.pagerOptions.filter).toBeNull();
          expect(this.controller.pagerOptions.status).toBe(this.context.possibleStatuses[0]);
        });

        it('updates items when name sort field is updated', function() {
          var sortFieldValue;

          this.createController();
          sortFieldValue = this.controller.sortFields[1];
          this.controller.sortFieldSelected(sortFieldValue);
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: this.context.possibleStatuses[0].id,
            page: 1,
            pageSize: this.context.pageSize,
            sort: sortFieldValue.toLowerCase()
          });
        });

        it('updates items when name page number is changed', function() {
          var page = 2;

          this.createController();
          this.controller.pagerOptions.page = page;
          this.controller.pageChanged();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: this.context.possibleStatuses[0].id,
            page: page,
            pageSize: this.context.pageSize,
            sort: this.controller.sortFields[0].toLowerCase()
          });
        });

        it('has valid display state when there are no entities for criteria', function() {
          var self = this;

          this.createController(getItemsWrapper);
          expect(this.controller.displayState).toBe(1); // NO_RESULTS

          function getItemsWrapper(options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    [],
              page:     options.page,
              pageSize: context.pageSize,
              offset:   0,
              total:    0
            });
          }
        });

        it('has valid display state when there are entities for criteria', function() {
          var self = this;

          this.createController(getItemsWrapper);
          expect(self.controller.displayState).toBe(2); // HAVE_RESULTS

          function getItemsWrapper(options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    context.entities.slice(0, context.pageSize),
              page:     options.page,
              pageSize: context.pageSize,
              offset:   0,
              total:    context.entities.length + 1
            });
          }
        });

        it('has valid display state when there are no entities', function() {
          this.context.counts = _.mapObject(this.context.counts, function (val) {
            return 0;
          });
          this.createController();
          expect(this.controller.displayState).toBe(0); // NO_ENTITIES
        });

      });

    }

    function createCounts(/* disabled, enabled, retired */) {
      var args = _.toArray(arguments),
          result = {};

      if (args.length < 1) {
        throw new Error('disabled count not specified');
      }

      result.disabled = args.shift();
      result.total = result.disabled;

      if (args.length < 1) {
        throw new Error('enabled count not specified');
      }

      result.enabled = args.shift();
      result.total += result.enabled;

      if (args.length > 0) {
        result.retired = args.shift();
        result.total += result.retired;
      }

      return result;
    }

  });

});
