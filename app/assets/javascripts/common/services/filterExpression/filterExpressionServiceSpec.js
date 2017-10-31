/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('filterExpressionService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('filterExpression');
    });
  });

  it('can create a filter expression', function() {
    var result = this.filterExpression.create([{ key: 'name', value: 'test' }]);
    expect(result).toEqual('name::test');
  });

  it('expressions with empty values are ignored', function() {
    var result = this.filterExpression.create([
      { key: 'key1', value: '' },
      { key: 'key2', value: 'val2' }
    ]);
    expect(result).toEqual('key2::val2');
  });

});
