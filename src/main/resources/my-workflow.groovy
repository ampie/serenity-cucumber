when 'Open', {
    'success' should: 'Resolve Issue'
}

when 'Reopened', {
    'success' should: 'Resolve Issue'
}

when 'Resolved', {
    'failure' should: 'Reopen Issue'
}

when 'In Progress', {
    'success' should: ['Done']
}

when 'Closed', {
    'failure' should: 'Reopen Issue'
}
