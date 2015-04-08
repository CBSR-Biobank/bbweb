/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Directive: statusLineDirective', function() {
    var element, createScope, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($templateCache, fakeDomainEntities) {
      testUtils.putHtmlTemplate($templateCache,
                                '/assets/javascripts/admin/statusLine.html');

      createScope = setupScope(this.$injector);
      fakeEntities = fakeDomainEntities;
    }));

    function setupScope(injector) {
      var $rootScope = injector.get('$rootScope'),
          $compile   = injector.get('$compile');

      return create;

      //--

      function create(entity) {
        var scope = $rootScope;

        element = angular.element('<status-line item="model.entity"></status-line>');
        scope.model = {};
        scope.model.entity = entity;

        $compile(element)(scope);
        scope.$digest();
        return scope;
      }
    }

    it('has the correct number of table cells', function() {
      var Centre = this.$injector.get('Centre'),
          $filter = this.$injector.get('$filter'),
          centre = new Centre(fakeEntities.centre()),
          cells;

      createScope(centre);
      cells = element.find('td');
      expect(cells.length).toBe(3);
      _.each(_.range(3), function (n) {
        var small = cells.eq(n).find('small');
        expect(small.eq(0)).toHaveClass('text-muted');
      });

      expect(cells.eq(0).find('small').eq(0).text()).toContain(centre.status);
      expect(cells.eq(1).find('small').eq(0).text()).toContain($filter('timeago')(centre.timeAdded));
    });

    it('displays correct value if timeModified has a value', function() {
      var Centre = this.$injector.get('Centre'),
          $filter = this.$injector.get('$filter'),
          centre = new Centre(fakeEntities.centre()),
          scope,
          cells;

      scope = createScope(centre);
      cells = element.find('td');

      expect(centre.timeModified).toBeDefined();
      expect(cells.eq(2).find('small').eq(0).text()).toContain($filter('timeago')(centre.timeModified));
    });

    it('displays correct value if timeModified is undefined', function() {
      var Centre = this.$injector.get('Centre'),
          centre = new Centre(fakeEntities.centre()),
          scope,
          cells;

      centre.timeModified = undefined;
      scope = createScope(centre);

      cells = element.find('td');
      expect(cells.eq(2).find('small').eq(0).text()).toContain('Never');
    });

  });

});
